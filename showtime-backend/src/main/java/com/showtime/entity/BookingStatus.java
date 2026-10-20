package com.showtime.entity;

public enum BookingStatus {
    PENDING,     // seats held, waiting for payment
    CONFIRMED,   // payment verified
    FAILED,
    CANCELLED,   // cancelled by the user before paying
    EXPIRED      // hold time ran out
}
