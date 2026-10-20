package com.showtime.entity;

import jakarta.persistence.*;

/**
 * One row = one seat held/booked for a show.
 * The UNIQUE constraint (show_id, seat_label) is what guarantees that two users can never
 * get the same seat, even if they click at the same moment. Rows are deleted when a booking
 * is cancelled or expires, which frees the seat again.
 */
@Entity
@Table(name = "seat_locks",
        uniqueConstraints = @UniqueConstraint(name = "uk_show_seat", columnNames = {"show_id", "seat_label"}))
public class SeatLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    @Column(name = "seat_label", nullable = false, length = 5)
    private String seatLabel;

    @ManyToOne(optional = false)
    private Booking booking;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getShowId() { return showId; }
    public void setShowId(Long showId) { this.showId = showId; }
    public String getSeatLabel() { return seatLabel; }
    public void setSeatLabel(String seatLabel) { this.seatLabel = seatLabel; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
}
