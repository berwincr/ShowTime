package com.showtime.controller;

import com.showtime.dto.FailRequest;
import com.showtime.dto.OrderRequest;
import com.showtime.dto.VerifyRequest;
import com.showtime.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** Needs login. Creates a Razorpay order for a PENDING booking and returns the checkout URL. */
    @PostMapping("/order")
    public Map<String, Object> order(@RequestAttribute("userId") Long userId, @Valid @RequestBody OrderRequest r) {
        return paymentService.createOrder(userId, r.bookingId());
    }

    /** Called by /pay.html. Public, but only a payment with a valid Razorpay signature can confirm a booking. */
    @PostMapping("/verify")
    public Map<String, Object> verify(@Valid @RequestBody VerifyRequest r) {
        return paymentService.verify(r.orderId(), r.paymentId(), r.signature());
    }

    @PostMapping("/failed")
    public Map<String, String> failed(@Valid @RequestBody FailRequest r) {
        paymentService.recordFailure(r.orderId(), r.reason());
        return Map.of("status", "recorded");
    }
}
