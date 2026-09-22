package com.example.payment.service;

import com.example.seat.model.SeatAllocationEvent;
import org.springframework.stereotype.Service;

@Service
public class PaymentProcessingService {

    public boolean processPayment(SeatAllocationEvent event) {
        // Mock payment processing
        return event != null && event.getTotalPrice() != null && event.getTotalPrice() > 0;
    }
}
