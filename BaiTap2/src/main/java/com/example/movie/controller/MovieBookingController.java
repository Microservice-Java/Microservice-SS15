package com.example.movie.controller;

import com.example.movie.model.CinemaBookingRequest;
import com.example.movie.service.BookingPublisherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class MovieBookingController {

    private final BookingPublisherService bookingPublisherService;

    public MovieBookingController(BookingPublisherService bookingPublisherService) {
        this.bookingPublisherService = bookingPublisherService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createBooking(@RequestBody CinemaBookingRequest request) {
        String correlationId = bookingPublisherService.createBooking(request);
        return ResponseEntity.ok(Map.of(
                "message", "Booking event created successfully",
                "cinemaBookingId", request.getCinemaBookingId(),
                "correlationId", correlationId
        ));
    }
}
