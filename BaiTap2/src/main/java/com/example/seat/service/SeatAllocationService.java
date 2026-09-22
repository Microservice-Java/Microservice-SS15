package com.example.seat.service;

import com.example.movie.model.CinemaBookingRequest;
import org.springframework.stereotype.Service;

@Service
public class SeatAllocationService {

    public String reserveSeats(CinemaBookingRequest request) {
        if (request.getSeatNumbers() != null && !request.getSeatNumbers().isEmpty()) {
            return String.join(", ", request.getSeatNumbers());
        }
        return "DEFAULT-SEAT";
    }
}
