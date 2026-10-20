package com.showtime.integration;

import java.time.LocalDate;
import java.time.LocalTime;

/** JSON returned by the show-service (port 8081). Field names must match its ShowDto. */
public record ShowInfo(Long id, Long movieId, String movieTitle, Long theatreId, String theatreName,
                       String area, LocalDate showDate, LocalTime showTime, int price,
                       int seatRows, int seatCols) {
}
