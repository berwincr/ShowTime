package com.showtime.dto;

import jakarta.validation.constraints.NotNull;

public record OrderRequest(@NotNull(message = "bookingId is required") Long bookingId) {
}
