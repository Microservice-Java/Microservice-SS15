package com.example.notification.consumer;

import com.example.notification.model.SeatReservedEvent;
import com.example.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SeatReservedConsumer {

    private static final Logger log = LoggerFactory.getLogger(SeatReservedConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Autowired
    public SeatReservedConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "seat-events", groupId = "notification-group")
    public void handleSeatReserved(ConsumerRecord<String, String> record) {
        try {
            SeatReservedEvent event = objectMapper.readValue(record.value(), SeatReservedEvent.class);
            String correlationId = event.getCorrelationId();
            String email = event.getCustomerEmail();

            log.info("[NotifyService] Received confirmation for correlationId: {} - Sending email to {}", correlationId, email);

            // Gửi email thông báo (mô phỏng)
            notificationService.sendConfirmationEmail(email, correlationId);

        } catch (Exception e) {
            log.error("Error processing seat reserved event: {}", e.getMessage(), e);
        }
    }
}
