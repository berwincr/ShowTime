package com.showtime.entity;

public enum PaymentStatus {
    CREATED,   // Razorpay order created, waiting for the customer
    SUCCESS,   // signature verified by the backend
    FAILED
}
