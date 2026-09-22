package com.example.concert;

import com.example.concert.model.ConcertBookingEvent;
import com.example.concert.service.BookingPublisherService;
import com.example.notification.consumer.SeatReservedConsumer;
import com.example.notification.model.SeatReservedEvent;
import com.example.seat.consumer.ConcertBookingConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.kafka.listener.auto-startup=false")
class ConcertSagaTest {

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private BookingPublisherService bookingPublisherService;

    @Autowired
    private ConcertBookingConsumer concertBookingConsumer;

    @Autowired
    private SeatReservedConsumer seatReservedConsumer;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Kiểm thử luồng Choreography Saga qua Kafka Topics cho Concert Booking")
    void testConcertBookingChoreographySaga() throws Exception {
        // Arrange: Input từ bài tập
        ConcertBookingEvent bookingEvent = new ConcertBookingEvent(
                "CONCERT-2024-999",
                "LIVE-HCM-2024",
                "nguyenvanA@email.com",
                3
        );

        // Step 1: ConcertBookingService phát sự kiện lên topic concert-events
        assertDoesNotThrow(() -> bookingPublisherService.publishConcertBooking(bookingEvent));

        // Step 2: SeatAssignmentConsumer tiêu thụ sự kiện từ topic concert-events
        String bookingJson = objectMapper.writeValueAsString(bookingEvent);
        ConsumerRecord<String, String> concertRecord = new ConsumerRecord<>(
                "concert-events", 0, 0L, bookingEvent.getCorrelationId(), bookingJson
        );

        assertDoesNotThrow(() -> concertBookingConsumer.handleConcertBooking(concertRecord));

        // Step 3: NotificationConsumer tiêu thụ sự kiện SeatReserved từ topic seat-events
        SeatReservedEvent seatReservedEvent = new SeatReservedEvent(
                "CONCERT-2024-999",
                "nguyenvanA@email.com",
                "LIVE-HCM-2024",
                3,
                "RESERVED"
        );
        String seatJson = objectMapper.writeValueAsString(seatReservedEvent);
        ConsumerRecord<String, String> seatRecord = new ConsumerRecord<>(
                "seat-events", 0, 0L, seatReservedEvent.getCorrelationId(), seatJson
        );

        assertDoesNotThrow(() -> seatReservedConsumer.handleSeatReserved(seatRecord));
    }
}
