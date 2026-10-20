package com.showtime.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyRequest(
        @NotBlank String orderId,
        @NotBlank String paymentId,
        @NotBlank String signature) {
}
