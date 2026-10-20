package com.showtime.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.showtime.entity.Booking;
import com.showtime.entity.BookingStatus;
import com.showtime.entity.Payment;
import com.showtime.entity.PaymentStatus;
import com.showtime.exception.ApiException;
import com.showtime.repository.BookingRepository;
import com.showtime.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Razorpay integration (test mode).
 * 1. createOrder  - backend creates a Razorpay Order (amount is decided by the SERVER, never the client).
 * 2. The customer pays on the Razorpay Checkout page (/pay.html) opened from the Swing app.
 * 3. verify       - backend checks the HMAC-SHA256 signature using the secret key. Only then is the booking CONFIRMED.
 */
@Service
public class PaymentService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final String ORDERS_URL = "https://api.razorpay.com/v1/orders";

    private final BookingRepository bookings;
    private final PaymentRepository payments;
    private final TransactionTemplate tx;
    private final RestClient http;
    private final String keyId;
    private final String keySecret;
    private final String publicUrl;

    public PaymentService(BookingRepository bookings, PaymentRepository payments, TransactionTemplate tx,
                          RestClient http,
                          @Value("${razorpay.key-id:}") String keyId,
                          @Value("${razorpay.key-secret:}") String keySecret,
                          @Value("${app.public-url}") String publicUrl) {
        this.bookings = bookings;
        this.payments = payments;
        this.tx = tx;
        this.http = http;
        this.keyId = keyId == null ? "" : keyId.trim();
        this.keySecret = keySecret == null ? "" : keySecret.trim();
        this.publicUrl = publicUrl;
    }

    public Map<String, Object> createOrder(Long userId, Long bookingId) {
        if (keyId.isEmpty() || keySecret.isEmpty()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Razorpay keys are not configured. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET (test keys).");
        }
        Booking b = bookings.findById(bookingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!b.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This booking belongs to another user");
        }
        LocalDateTime now = LocalDateTime.now(IST);
        if (b.getStatus() != BookingStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "This booking is " + b.getStatus() + " and cannot be paid");
        }
        if (b.getExpiresAt().isBefore(now)) {
            throw new ApiException(HttpStatus.CONFLICT, "Your seat hold has expired. Please select seats again.");
        }

        Payment p = payments.findByBookingId(bookingId).orElse(null);
        if (p == null) {
            String orderId = createRazorpayOrder(b);
            p = new Payment();
            p.setBooking(b);
            p.setRazorpayOrderId(orderId);
            p.setAmount(b.getAmount());
            p.setStatus(PaymentStatus.CREATED);
            p.setCreatedAt(now);
            payments.save(p);
        } else if (p.getStatus() == PaymentStatus.FAILED) {
            p.setStatus(PaymentStatus.CREATED);      // retry with the same Razorpay order
            payments.save(p);
        }

        // give the customer a little more time while paying
        b.setExpiresAt(now.plusMinutes(8));
        bookings.save(b);

        long paise = b.getAmount() * 100L;
        String payUrl = publicUrl + "/pay.html"
                + "?orderId=" + enc(p.getRazorpayOrderId())
                + "&key=" + enc(keyId)
                + "&amount=" + paise
                + "&bookingId=" + b.getId()
                + "&desc=" + enc(b.getMovieTitle() + " | " + b.getTheatreName() + " | Seats " + b.getSeats())
                + "&name=" + enc(b.getUser().getName())
                + "&email=" + enc(b.getUser().getEmail());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("orderId", p.getRazorpayOrderId());
        out.put("keyId", keyId);
        out.put("amountPaise", paise);
        out.put("bookingId", b.getId());
        out.put("payUrl", payUrl);
        return out;
    }

    /** Called by the checkout page after the customer pays. The signature is the proof of payment. */
    public Map<String, Object> verify(String orderId, String paymentId, String signature) {
        Payment p = payments.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Unknown payment order"));

        String expected = hmacSha256Hex(orderId + "|" + paymentId, keySecret);
        boolean valid = MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
        if (!valid) {
            recordFailure(orderId, "Signature verification failed");
            throw new ApiException(HttpStatus.BAD_REQUEST, "Payment verification failed");
        }

        return tx.execute(status -> {
            Payment pay = payments.findById(p.getId()).orElseThrow();
            Booking b = pay.getBooking();
            pay.setRazorpayPaymentId(paymentId);
            pay.setStatus(PaymentStatus.SUCCESS);
            pay.setFailureReason(null);
            payments.save(pay);

            if (b.getStatus() == BookingStatus.PENDING) {
                b.setStatus(BookingStatus.CONFIRMED);
                bookings.save(b);
                return result("CONFIRMED", b.getId(), "Payment successful. Booking confirmed!");
            }
            if (b.getStatus() == BookingStatus.CONFIRMED) {
                return result("CONFIRMED", b.getId(), "Booking already confirmed.");
            }
            return result(b.getStatus().name(), b.getId(),
                    "Payment received but the seat hold had expired. Please contact support for a refund.");
        });
    }

    /** Called by the checkout page when Razorpay reports a failed attempt. The booking stays PENDING so the user can retry. */
    public void recordFailure(String orderId, String reason) {
        payments.findByRazorpayOrderId(orderId).ifPresent(p -> {
            if (p.getStatus() != PaymentStatus.SUCCESS) {
                p.setStatus(PaymentStatus.FAILED);
                p.setFailureReason(reason == null ? "Payment failed" : reason);
                payments.save(p);
            }
        });
    }

    // ------------------------------------------------------------------

    private String createRazorpayOrder(Booking b) {
        String basic = Base64.getEncoder().encodeToString((keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", b.getAmount() * 100L);          // paise
        body.put("currency", "INR");
        body.put("receipt", "booking_" + b.getId());
        try {
            JsonNode res = http.post()
                    .uri(ORDERS_URL)
                    .header("Authorization", "Basic " + basic)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            if (res == null || !res.hasNonNull("id")) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Razorpay did not return an order id");
            }
            return res.get("id").asText();
        } catch (RestClientResponseException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Razorpay error " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString());
        }
    }

    private static String hmacSha256Hex(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Could not compute HMAC", e);
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    private static Map<String, Object> result(String status, Long bookingId, String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", status);
        m.put("bookingId", bookingId);
        m.put("message", message);
        return m;
    }
}
