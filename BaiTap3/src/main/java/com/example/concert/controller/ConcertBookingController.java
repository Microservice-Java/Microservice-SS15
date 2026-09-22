package com.example.concert.controller;

import com.example.concert.model.ConcertBookingEvent;
import com.example.concert.service.BookingPublisherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/concert/bookings")
public class ConcertBookingController {

    private final BookingPublisherService bookingPublisherService;

    public ConcertBookingController(BookingPublisherService bookingPublisherService) {
        this.bookingPublisherService = bookingPublisherService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createBooking(@RequestBody ConcertBookingEvent event) {
        bookingPublisherService.publishConcertBooking(event);
        return ResponseEntity.ok(Map.of(
                "message", "Concert booking event published successfully",
                "correlationId", event.getCorrelationId()
        ));
    }
}
