package com.showtime.controller;

import com.showtime.dto.BookingDto;
import com.showtime.dto.BookingRequest;
import com.showtime.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** All endpoints here require "Authorization: Bearer <token>" (see AuthInterceptor). */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto create(@RequestAttribute("userId") Long userId, @Valid @RequestBody BookingRequest r) {
        return bookingService.create(userId, r.showId(), r.seats());
    }

    @GetMapping("/my")
    public List<BookingDto> mine(@RequestAttribute("userId") Long userId) {
        return bookingService.history(userId);
    }

    @GetMapping("/{id}")
    public BookingDto one(@RequestAttribute("userId") Long userId, @PathVariable("id") Long id) {
        return bookingService.get(userId, id);
    }

    @PostMapping("/{id}/cancel")
    public BookingDto cancel(@RequestAttribute("userId") Long userId, @PathVariable("id") Long id) {
        return bookingService.cancel(userId, id);
    }
}
