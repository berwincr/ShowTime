package com.showtime.dto;

import jakarta.validation.constraints.NotBlank;

public record FailRequest(@NotBlank String orderId, String reason) {
}
