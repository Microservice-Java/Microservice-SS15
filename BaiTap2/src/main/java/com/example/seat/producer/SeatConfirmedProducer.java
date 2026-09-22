package com.example.seat.producer;

import com.example.seat.model.SeatAllocationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SeatConfirmedProducer {

    private static final Logger log = LoggerFactory.getLogger(SeatConfirmedProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public SeatConfirmedProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishSeatConfirmed(SeatAllocationEvent event, String correlationId) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            ProducerRecord<String, String> record = new ProducerRecord<>(
                    "seat-confirmed-events",
                    event.getCinemaBookingId(),
                    payload
            );

            // Gắn correlationId tiếp tục vào HEADER của sự kiện tiếp theo
            record.headers().add("correlationId", correlationId.getBytes(StandardCharsets.UTF_8));

            if (kafkaTemplate != null) {
                try {
                    kafkaTemplate.send(record);
                } catch (Exception e) {
                    log.warn("Kafka broker offline, skipped async send: {}", e.getMessage());
                }
            }

        } catch (JsonProcessingException e) {
            log.error("Error serializing seat confirmed event", e);
        }
    }
}
