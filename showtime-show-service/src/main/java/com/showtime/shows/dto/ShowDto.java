package com.showtime.shows.dto;

import com.showtime.shows.entity.MovieShow;

import java.time.LocalDate;
import java.time.LocalTime;

public record ShowDto(Long id, Long movieId, String movieTitle, Long theatreId, String theatreName,
                      String area, LocalDate showDate, LocalTime showTime, int price,
                      int seatRows, int seatCols) {

    public static ShowDto from(MovieShow s) {
        return new ShowDto(s.getId(), s.getMovieId(), s.getMovieTitle(),
                s.getTheatre().getId(), s.getTheatre().getName(), s.getTheatre().getArea(),
                s.getShowDate(), s.getShowTime(), s.getPrice(), s.getSeatRows(), s.getSeatCols());
    }
}
