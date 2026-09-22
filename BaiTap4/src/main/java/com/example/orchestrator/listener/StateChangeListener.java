package com.example.orchestrator.listener;

import com.example.orchestrator.model.BookingEvent;
import com.example.orchestrator.model.BookingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StateChangeListener {

    private static final Logger log = LoggerFactory.getLogger(StateChangeListener.class);

    public void onStateChanged(String bookingId, BookingState oldState, BookingState newState, BookingEvent event) {
        log.info("[Orchestrator] State: {} -> Event: {} -> New State: {}", oldState, event, newState);
    }
}
