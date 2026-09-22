package com.example.orchestrator.service;

import com.example.orchestrator.model.BookingTransaction;
import com.example.payment.service.PaymentProcessingService;
import org.springframework.stereotype.Service;

@Service
public class PaymentOrchestrationService {

    private final PaymentProcessingService paymentProcessingService;

    public PaymentOrchestrationService(PaymentProcessingService paymentProcessingService) {
        this.paymentProcessingService = paymentProcessingService;
    }

    public boolean processPayment(BookingTransaction transaction) throws Exception {
        return paymentProcessingService.processPayment(transaction);
    }

    public void refund(BookingTransaction transaction) {
        paymentProcessingService.refund(transaction);
    }
}
