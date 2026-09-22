package com.example.concert.service;

import com.example.concert.model.ConcertBookingEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BookingPublisherService {

    private static final Logger log = LoggerFactory.getLogger(BookingPublisherService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public BookingPublisherService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishConcertBooking(ConcertBookingEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            log.info("[ConcertBookingService] Publishing ConcertBookingEvent for correlationId: {} to topic: concert-events",
                    event.getCorrelationId());

            if (kafkaTemplate != null) {
                try {
                    kafkaTemplate.send("concert-events", event.getCorrelationId(), payload);
                } catch (Exception e) {
                    log.warn("[ConcertBookingService] Kafka broker offline, skipped async send: {}", e.getMessage());
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Error serializing concert booking event", e);
        }
    }
}
