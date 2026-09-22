package com.example.seat.producer;

import com.example.seat.model.SeatReservedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SeatEventProducer {

    private static final Logger log = LoggerFactory.getLogger(SeatEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public SeatEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishSeatReserved(SeatReservedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            if (kafkaTemplate != null) {
                try {
                    kafkaTemplate.send("seat-events", event.getCorrelationId(), payload);
                } catch (Exception e) {
                    log.warn("[SeatService] Kafka broker offline, skipped async send: {}", e.getMessage());
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Error serializing seat reserved event", e);
        }
    }
}
