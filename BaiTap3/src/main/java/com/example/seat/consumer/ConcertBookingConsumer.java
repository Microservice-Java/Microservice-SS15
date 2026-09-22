package com.example.seat.consumer;

import com.example.seat.model.ConcertBookingEvent;
import com.example.seat.model.SeatReservedEvent;
import com.example.seat.producer.SeatEventProducer;
import com.example.seat.service.SeatAssignmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ConcertBookingConsumer {

    private static final Logger log = LoggerFactory.getLogger(ConcertBookingConsumer.class);

    private final SeatAssignmentService seatService;
    private final SeatEventProducer seatEventProducer;
    private final ObjectMapper objectMapper;

    @Autowired
    public ConcertBookingConsumer(SeatAssignmentService seatService,
                                  SeatEventProducer seatEventProducer,
                                  ObjectMapper objectMapper) {
        this.seatService = seatService;
        this.seatEventProducer = seatEventProducer;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "concert-events", groupId = "seat-assignment-group")
    public void handleConcertBooking(ConsumerRecord<String, String> record) {
        try {
            ConcertBookingEvent event = objectMapper.readValue(record.value(), ConcertBookingEvent.class);
            String correlationId = event.getCorrelationId();

            log.info("[SeatService] Received event with correlationId: {}", correlationId);

            // Mô phỏng giữ chỗ ghế
            seatService.reserveSeat(event);
            log.info("[SeatService] Seat reserved successfully for correlationId: {}", correlationId);

            // Tạo sự kiện SeatReserved và gán đúng correlationId
            SeatReservedEvent seatEvent = new SeatReservedEvent();
            seatEvent.setCorrelationId(correlationId);
            seatEvent.setCustomerEmail(event.getCustomerEmail());
            seatEvent.setConcertCode(event.getConcertCode());
            seatEvent.setTicketQuantity(event.getTicketQuantity());
            seatEvent.setStatus("RESERVED");

            seatEventProducer.publishSeatReserved(seatEvent);
            log.info("[SeatService] Publishing SeatReserved event with correlationId: {} to topic: seat-events", correlationId);

        } catch (Exception e) {
            log.error("Error processing concert booking: {}", e.getMessage(), e);
        }
    }
}
