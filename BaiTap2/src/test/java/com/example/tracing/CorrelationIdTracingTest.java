package com.example.tracing;

import com.example.movie.model.CinemaBookingRequest;
import com.example.movie.service.BookingPublisherService;
import com.example.payment.consumer.SeatConfirmedConsumer;
import com.example.seat.consumer.BookingEventConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest(properties = "spring.kafka.listener.auto-startup=false")
class CorrelationIdTracingTest {

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private BookingPublisherService bookingPublisherService;

    @Autowired
    private BookingEventConsumer bookingEventConsumer;

    @Autowired
    private SeatConfirmedConsumer seatConfirmedConsumer;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Kiểm thử luồng Correlation ID xuyên suốt qua Header của Kafka tin nhắn")
    void testCorrelationIdHeaderPropagation() throws Exception {
        // Arrange: Data input từ bài tập
        CinemaBookingRequest request = new CinemaBookingRequest(
                "CIN-2024-789",
                "AVENGERS-5",
                "2024-12-25T19:30:00",
                List.of("A12", "A13"),
                "tuananh@email.com",
                240000.0
        );

        String correlationId = "550e8400-e29b-41d4-a716-446655440000";

        // Step 1: MovieBookingService khởi tạo booking & tạo correlationId
        String generatedCorrelationId = bookingPublisherService.createBooking(request, correlationId);
        assertEquals(correlationId, generatedCorrelationId);

        // Step 2: Mô phỏng SeatAllocationService nhận message với Header correlationId
        RecordHeaders headers = new RecordHeaders();
        headers.add(new RecordHeader("correlationId", correlationId.getBytes(StandardCharsets.UTF_8)));

        String bookingJson = objectMapper.writeValueAsString(request);
        ConsumerRecord<String, String> bookingRecord = new ConsumerRecord<>(
                "booking-events", 0, 0L, request.getCinemaBookingId(), bookingJson
        );
        headers.forEach(h -> bookingRecord.headers().add(h));

        // Act: Execute SeatAllocationConsumer
        assertDoesNotThrow(() -> bookingEventConsumer.handleBooking(bookingRecord));

        // Step 3: Mô phỏng PaymentService nhận message từ topic seat-confirmed-events với Header correlationId
        String seatEventJson = "{\"cinemaBookingId\":\"CIN-2024-789\",\"movieCode\":\"AVENGERS-5\",\"seatNumbers\":[\"A12\",\"A13\"],\"customerEmail\":\"tuananh@email.com\",\"totalPrice\":240000.0,\"seatStatus\":\"RESERVED\"}";
        ConsumerRecord<String, String> seatConfirmedRecord = new ConsumerRecord<>(
                "seat-confirmed-events", 0, 0L, request.getCinemaBookingId(), seatEventJson
        );
        headers.forEach(h -> seatConfirmedRecord.headers().add(h));

        // Act: Execute PaymentConsumer
        assertDoesNotThrow(() -> seatConfirmedConsumer.handleSeatConfirmed(seatConfirmedRecord));
    }
}
