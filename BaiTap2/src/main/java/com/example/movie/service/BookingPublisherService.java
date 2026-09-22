package com.example.movie.service;

import com.example.movie.model.CinemaBookingRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

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

    public String createBooking(CinemaBookingRequest request) {
        String correlationId = UUID.randomUUID().toString();
        return createBooking(request, correlationId);
    }

    public String createBooking(CinemaBookingRequest request, String correlationId) {
        log.info("[MovieBookingService] Created booking {}. CorrelationID: {}",
                request.getCinemaBookingId(), correlationId);

        try {
            String payload = objectMapper.writeValueAsString(request);

            ProducerRecord<String, String> record = new ProducerRecord<>(
                    "booking-events",
                    request.getCinemaBookingId(),
                    payload
            );

            // Gắn correlationId vào HEADER - KHÔNG đặt trong payload
            record.headers().add("correlationId", correlationId.getBytes(StandardCharsets.UTF_8));

            if (kafkaTemplate != null) {
                try {
                    kafkaTemplate.send(record);
                } catch (Exception e) {
                    log.warn("Kafka broker offline, skipped async send: {}", e.getMessage());
                }
            }

        } catch (JsonProcessingException e) {
            log.error("Error serializing booking request", e);
        }

        return correlationId;
    }
}
