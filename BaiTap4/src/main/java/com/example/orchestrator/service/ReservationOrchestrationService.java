package com.example.orchestrator.service;

import com.example.concert.service.SeatReservationService;
import com.example.orchestrator.model.BookingTransaction;
import org.springframework.stereotype.Service;

@Service
public class ReservationOrchestrationService {

    private final SeatReservationService seatReservationService;

    public ReservationOrchestrationService(SeatReservationService seatReservationService) {
        this.seatReservationService = seatReservationService;
    }

    public boolean reserveSeats(BookingTransaction transaction) {
        return seatReservationService.reserveSeats(transaction);
    }

    public void releaseSeats(BookingTransaction transaction) {
        seatReservationService.releaseSeats(transaction);
    }
}
