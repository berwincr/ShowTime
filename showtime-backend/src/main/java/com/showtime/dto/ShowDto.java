package com.showtime.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/** A show as seen by the Swing client: show-service data + live seat availability. */
public record ShowDto(Long showId, Long movieId, String movieTitle, Long theatreId, String theatreName,
                      String area, LocalDate showDate, LocalTime showTime, int price,
                      int totalSeats, int availableSeats) {
}
