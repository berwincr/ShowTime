package com.showtime.dto;

import java.time.LocalDateTime;

public record BookingDto(Long id, String status, Long showId, Long movieId, String movieTitle,
                         String theatreName, LocalDateTime showTime, String seats, int seatCount,
                         int amount, LocalDateTime createdAt, LocalDateTime expiresAt,
                         String paymentStatus, String razorpayPaymentId, boolean upcoming) {
}
