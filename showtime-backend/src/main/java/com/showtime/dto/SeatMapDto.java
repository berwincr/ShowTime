package com.showtime.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SeatMapDto(Long showId, String movieTitle, String theatreName, LocalDateTime showTime,
                         int price, int rows, int cols, List<String> bookedSeats) {
}
