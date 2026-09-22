package com.example.payment.consumer;

import com.example.payment.service.PaymentProcessingService;
import com.example.seat.model.SeatAllocationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SeatConfirmedConsumer {

    private static final Logger log = LoggerFactory.getLogger(SeatConfirmedConsumer.class);

    private final PaymentProcessingService paymentService;
    private final ObjectMapper objectMapper;

    @Autowired
    public SeatConfirmedConsumer(PaymentProcessingService paymentService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "seat-confirmed-events", groupId = "payment-group")
    public void handleSeatConfirmed(ConsumerRecord<String, String> record) {
        // Trích xuất correlationId từ HEADER
        String correlationId = extractCorrelationId(record);

        log.info("[PaymentService] Processing Payment for {}. CorrelationID: {}",
                record.key(), correlationId);

        try {
            SeatAllocationEvent event = objectMapper.readValue(record.value(), SeatAllocationEvent.class);
            boolean success = paymentService.processPayment(event);

            if (success) {
                log.info("[PaymentService] Payment success: {} VND. CorrelationID: {}",
                        event.getTotalPrice().longValue(), correlationId);
            }

        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage(), e);
        }
    }

    private String extractCorrelationId(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader("correlationId");
        if (header != null && header.value() != null) {
            return new String(header.value(), StandardCharsets.UTF_8);
        }
        return "UNKNOWN_CORRELATION_ID";
    }
}
