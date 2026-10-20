package com.showtime.service;

import com.showtime.dto.SeatMapDto;
import com.showtime.dto.ShowDto;
import com.showtime.entity.Movie;
import com.showtime.exception.ApiException;
import com.showtime.integration.ShowClient;
import com.showtime.integration.ShowInfo;
import com.showtime.repository.SeatLockRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/** Combines show data from the show-service with live seat availability from our own database. */
@Service
public class CatalogService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final MovieService movieService;
    private final ShowClient showClient;
    private final SeatLockRepository locks;

    public CatalogService(MovieService movieService, ShowClient showClient, SeatLockRepository locks) {
        this.movieService = movieService;
        this.showClient = showClient;
        this.locks = locks;
    }

    public List<ShowDto> showsForMovie(Long movieId, LocalDate date) {
        Movie movie = movieService.get(movieId);
        if (movie == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Movie not found");
        }
        showClient.ensureShows(movie.getId(), movie.getTitle());

        LocalDate day = (date == null) ? LocalDate.now(IST) : date;
        LocalDateTime now = LocalDateTime.now(IST);

        return showClient.shows(movieId, day).stream()
                .filter(s -> LocalDateTime.of(s.showDate(), s.showTime()).isAfter(now))
                .map(s -> {
                    int total = s.seatRows() * s.seatCols();
                    int taken = (int) locks.countByShowId(s.id());
                    return new ShowDto(s.id(), s.movieId(), s.movieTitle(), s.theatreId(), s.theatreName(),
                            s.area(), s.showDate(), s.showTime(), s.price(), total, Math.max(0, total - taken));
                })
                .toList();
    }

    public SeatMapDto seatMap(Long showId) {
        ShowInfo s = showClient.show(showId);
        return new SeatMapDto(s.id(), s.movieTitle(), s.theatreName(),
                LocalDateTime.of(s.showDate(), s.showTime()), s.price(), s.seatRows(), s.seatCols(),
                locks.findSeatLabels(showId));
    }
}
