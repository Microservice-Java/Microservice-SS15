package com.example.orchestrator.machine;

import com.example.orchestrator.model.BookingTransaction;

public interface ConcertBookingStateMachine {

    void process(BookingTransaction transaction);
}
