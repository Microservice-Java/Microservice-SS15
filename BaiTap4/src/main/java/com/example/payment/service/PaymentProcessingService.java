package com.example.payment.service;

import com.example.orchestrator.model.BookingTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessingService.class);

    private boolean failPayment = false;
    private int failAttemptsCount = 0; // Simulate transient failure for retries

    public void setFailPayment(boolean failPayment) {
        this.failPayment = failPayment;
    }

    public void setFailAttemptsCount(int failAttemptsCount) {
        this.failAttemptsCount = failAttemptsCount;
    }

    public boolean processPayment(BookingTransaction transaction) throws Exception {
        log.info("[PaymentService] Processing payment of {} VND for booking {}",
                transaction.getAmount(), transaction.getBookingId());

        if (failAttemptsCount > 0) {
            failAttemptsCount--;
            log.warn("[PaymentService] Simulating transient network timeout during payment!");
            throw new RuntimeException("Payment Service Network Timeout");
        }

        if (failPayment) {
            log.error("[PaymentService] Payment failed: Insufficient funds or invalid card!");
            return false;
        }

        log.info("[PaymentService] Payment processed successfully for booking {}", transaction.getBookingId());
        return true;
    }

    public void refund(BookingTransaction transaction) {
        log.info("[PaymentService] Refunded {} VND for booking {}",
                transaction.getAmount(), transaction.getBookingId());
    }
}
