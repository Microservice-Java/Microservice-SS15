package com.example.seat.consumer;

import com.example.movie.model.CinemaBookingRequest;
import com.example.seat.model.SeatAllocationEvent;
import com.example.seat.producer.SeatConfirmedProducer;
import com.example.seat.service.SeatAllocationService;
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
public class BookingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventConsumer.class);

    private final SeatAllocationService seatService;
    private final SeatConfirmedProducer seatConfirmedProducer;
    private final ObjectMapper objectMapper;

    @Autowired
    public BookingEventConsumer(SeatAllocationService seatService,
                                SeatConfirmedProducer seatConfirmedProducer,
                                ObjectMapper objectMapper) {
        this.seatService = seatService;
        this.seatConfirmedProducer = seatConfirmedProducer;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "booking-events", groupId = "seat-group")
    public void handleBooking(ConsumerRecord<String, String> record) {
        // Trích xuất correlationId từ HEADER - KHÔNG lấy từ payload
        String correlationId = extractCorrelationId(record);

        log.info("[SeatAllocationService] Received SeatRequest for {}. CorrelationID: {}",
                record.key(), correlationId);

        try {
            CinemaBookingRequest request = objectMapper.readValue(record.value(), CinemaBookingRequest.class);
            String seatResult = seatService.reserveSeats(request);

            log.info("[SeatAllocationService] Seat reserved: {}. CorrelationID: {}",
                    seatResult, correlationId);

            SeatAllocationEvent event = new SeatAllocationEvent(
                    request.getCinemaBookingId(),
                    request.getMovieCode(),
                    request.getSeatNumbers(),
                    request.getCustomerEmail(),
                    request.getTotalPrice(),
                    "RESERVED"
            );

            // Gửi sự kiện tiếp theo kèm correlationId trong header
            seatConfirmedProducer.publishSeatConfirmed(event, correlationId);

        } catch (Exception e) {
            log.error("Error processing booking: {}", e.getMessage(), e);
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
