package com.showtime.service;

import com.showtime.dto.BookingDto;
import com.showtime.entity.Booking;
import com.showtime.entity.BookingStatus;
import com.showtime.entity.Payment;
import com.showtime.entity.SeatLock;
import com.showtime.entity.User;
import com.showtime.exception.ApiException;
import com.showtime.integration.ShowClient;
import com.showtime.integration.ShowInfo;
import com.showtime.repository.BookingRepository;
import com.showtime.repository.PaymentRepository;
import com.showtime.repository.SeatLockRepository;
import com.showtime.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class BookingService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final Pattern SEAT = Pattern.compile("^[A-Z][0-9]{1,2}$");
    private static final int MAX_SEATS = 6;

    private final BookingRepository bookings;
    private final SeatLockRepository locks;
    private final PaymentRepository payments;
    private final UserRepository users;
    private final ShowClient showClient;
    private final TransactionTemplate tx;
    private final int holdMinutes;

    public BookingService(BookingRepository bookings, SeatLockRepository locks, PaymentRepository payments,
                          UserRepository users, ShowClient showClient, TransactionTemplate tx,
                          @Value("${app.seat-hold-minutes:5}") int holdMinutes) {
        this.bookings = bookings;
        this.locks = locks;
        this.payments = payments;
        this.users = users;
        this.showClient = showClient;
        this.tx = tx;
        this.holdMinutes = holdMinutes;
    }

    /** Creates a PENDING booking and holds the seats. */
    public BookingDto create(Long userId, Long showId, List<String> requestedSeats) {
        List<String> seats = requestedSeats.stream()
                .map(s -> s == null ? "" : s.trim().toUpperCase())
                .distinct()
                .toList();
        if (seats.isEmpty() || seats.size() > MAX_SEATS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please select 1 to " + MAX_SEATS + " seats");
        }
        for (String s : seats) {
            if (!SEAT.matcher(s).matches()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid seat: " + s);
            }
        }

        ShowInfo show = showClient.show(showId);
        for (String s : seats) {
            int row = s.charAt(0) - 'A';
            int col = Integer.parseInt(s.substring(1));
            if (row >= show.seatRows() || col < 1 || col > show.seatCols()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Seat " + s + " does not exist in this screen");
            }
        }
        LocalDateTime showTime = LocalDateTime.of(show.showDate(), show.showTime());
        LocalDateTime now = LocalDateTime.now(IST);
        if (!showTime.isAfter(now)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This show has already started");
        }

        releaseExpired();
        if (locks.existsByShowIdAndSeatLabelIn(showId, seats)) {
            throw new ApiException(HttpStatus.CONFLICT, "Some of those seats were just taken. Please pick again.");
        }
        User user = users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please log in again"));

        List<String> sorted = seats.stream()
                .sorted(Comparator.comparing((String s) -> s.charAt(0))
                        .thenComparingInt(s -> Integer.parseInt(s.substring(1))))
                .toList();

        try {
            return tx.execute(status -> {
                Booking b = new Booking();
                b.setUser(user);
                b.setShowId(showId);
                b.setMovieId(show.movieId());
                b.setMovieTitle(show.movieTitle());
                b.setTheatreName(show.theatreName());
                b.setShowTime(showTime);
                b.setSeats(String.join(",", sorted));
                b.setSeatCount(sorted.size());
                b.setAmount(show.price() * sorted.size());
                b.setStatus(BookingStatus.PENDING);
                b.setCreatedAt(now);
                b.setExpiresAt(now.plusMinutes(holdMinutes));
                bookings.save(b);

                for (String seat : sorted) {
                    SeatLock lock = new SeatLock();
                    lock.setShowId(showId);
                    lock.setSeatLabel(seat);
                    lock.setBooking(b);
                    locks.saveAndFlush(lock);   // UNIQUE(show_id, seat_label) stops double booking
                }
                return toDto(b);
            });
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.CONFLICT, "Some of those seats were just taken. Please pick again.");
        }
    }

    public BookingDto get(Long userId, Long bookingId) {
        return toDto(owned(userId, bookingId));
    }

    public List<BookingDto> history(Long userId) {
        return bookings.findHistory(userId).stream().map(this::toDto).toList();
    }

    /** A user can cancel a booking only while it is still unpaid. */
    public BookingDto cancel(Long userId, Long bookingId) {
        Booking b = owned(userId, bookingId);
        if (b.getStatus() == BookingStatus.CONFIRMED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Paid tickets cannot be cancelled in this demo");
        }
        if (b.getStatus() == BookingStatus.PENDING) {
            tx.executeWithoutResult(s -> {
                b.setStatus(BookingStatus.CANCELLED);
                bookings.save(b);
                locks.deleteByBookingId(b.getId());
            });
        }
        return toDto(owned(userId, bookingId));
    }

    /** Frees seats of unpaid bookings whose hold time ran out. */
    public void releaseExpired() {
        List<Booking> stale = bookings.findExpired(BookingStatus.PENDING, LocalDateTime.now(IST));
        for (Booking b : stale) {
            tx.executeWithoutResult(s -> {
                b.setStatus(BookingStatus.EXPIRED);
                bookings.save(b);
                locks.deleteByBookingId(b.getId());
            });
        }
    }

    @Scheduled(fixedDelay = 30_000)
    public void scheduledRelease() {
        releaseExpired();
    }

    // ------------------------------------------------------------------

    private Booking owned(Long userId, Long bookingId) {
        Booking b = bookings.findById(bookingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!b.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This booking belongs to another user");
        }
        return b;
    }

    private BookingDto toDto(Booking b) {
        Payment p = payments.findByBookingId(b.getId()).orElse(null);
        boolean upcoming = b.getStatus() == BookingStatus.CONFIRMED
                && b.getShowTime().isAfter(LocalDateTime.now(IST));
        return new BookingDto(b.getId(), b.getStatus().name(), b.getShowId(), b.getMovieId(), b.getMovieTitle(),
                b.getTheatreName(), b.getShowTime(), b.getSeats(), b.getSeatCount(), b.getAmount(),
                b.getCreatedAt(), b.getExpiresAt(),
                p == null ? null : p.getStatus().name(),
                p == null ? null : p.getRazorpayPaymentId(),
                upcoming);
    }
}
