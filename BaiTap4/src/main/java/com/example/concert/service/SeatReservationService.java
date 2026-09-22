package com.example.concert.service;

import com.example.orchestrator.model.BookingTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SeatReservationService {

    private static final Logger log = LoggerFactory.getLogger(SeatReservationService.class);

    private boolean failReservation = false;

    public void setFailReservation(boolean failReservation) {
        this.failReservation = failReservation;
    }

    public boolean reserveSeats(BookingTransaction transaction) {
        log.info("[ConcertService] Reserving {} seats for concert {} (Booking ID: {})",
                transaction.getTicketQuantity(), transaction.getConcertCode(), transaction.getBookingId());

        if (failReservation) {
            log.error("[ConcertService] Reservation failed: Seats fully booked!");
            return false;
        }

        log.info("[ConcertService] Seats reserved successfully for booking {}", transaction.getBookingId());
        return true;
    }

    public void releaseSeats(BookingTransaction transaction) {
        log.info("[ConcertService] Released reserved seats for booking {}", transaction.getBookingId());
    }
}
