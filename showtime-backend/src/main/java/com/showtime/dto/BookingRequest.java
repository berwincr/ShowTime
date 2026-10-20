package com.showtime.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BookingRequest(
        @NotNull(message = "showId is required") Long showId,
        @NotEmpty(message = "Select at least one seat") List<String> seats) {
}
