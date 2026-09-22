package com.example.orchestrator;

import com.example.concert.service.SeatReservationService;
import com.example.orchestrator.machine.ConcertBookingStateMachine;
import com.example.orchestrator.model.BookingState;
import com.example.orchestrator.model.BookingTransaction;
import com.example.payment.service.PaymentProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrchestratorStateMachineTest {

    @Autowired
    private ConcertBookingStateMachine stateMachine;

    @Autowired
    private PaymentProcessingService paymentProcessingService;

    @Autowired
    private SeatReservationService seatReservationService;

    @BeforeEach
    void setUp() {
        paymentProcessingService.setFailPayment(false);
        paymentProcessingService.setFailAttemptsCount(0);
        seatReservationService.setFailReservation(false);
    }

    @Test
    @DisplayName("Kịch bản 1: Happy Path -> Process Payment -> Payment Completed -> Reserve Seats -> Booking Confirmed")
    void testHappyPath_BookingConfirmed_MatchesExpectedOutputLog() {
        // Arrange: Input từ bài tập
        BookingTransaction transaction = new BookingTransaction(
                "CONCERT-2026-088",
                "LIVE-HCM-2026-ULTRA",
                "VIP-2024",
                "rika@email.com",
                3,
                5500000.0
        );

        // Act
        stateMachine.process(transaction);

        // Assert
        assertEquals(BookingState.BOOKING_CONFIRMED, transaction.getCurrentState(),
                "Trạng thái cuối cùng của giao dịch phải là BOOKING_CONFIRMED");
    }

    @Test
    @DisplayName("Kịch bản 2: Thanh toán bị timeout quá 3 lần thử -> Chuyển sang CANCELLED")
    void testPaymentFailureAfter3Retries_StateTransitionsToCancelled() {
        // Arrange: Giả lập Payment Service bị lỗi mạng 3 lần liên tiếp
        paymentProcessingService.setFailAttemptsCount(3);

        BookingTransaction transaction = new BookingTransaction(
                "CONCERT-2026-089",
                "LIVE-HCM-2026-ULTRA",
                "VIP-2024",
                "rika@email.com",
                2,
                3000000.0
        );

        // Act
        stateMachine.process(transaction);

        // Assert
        assertEquals(BookingState.CANCELLED, transaction.getCurrentState(),
                "Thanh toán thất bại sau 3 lần retry phải chuyển trạng thái sang CANCELLED");
    }

    @Test
    @DisplayName("Kịch bản 3: Giữ chỗ bị lỗi sau khi đã thanh toán -> Kích hoạt Bù trừ (Refund) và CANCELLED")
    void testReservationFailureAfterPayment_TriggersCompensationRefundAndCancels() {
        // Arrange: Thanh toán thành công nhưng giữ chỗ ghế thất bại (hết vé)
        seatReservationService.setFailReservation(true);

        BookingTransaction transaction = new BookingTransaction(
                "CONCERT-2026-090",
                "LIVE-HCM-2026-ULTRA",
                "VIP-2024",
                "rika@email.com",
                1,
                1500000.0
        );

        // Act
        stateMachine.process(transaction);

        // Assert
        assertEquals(BookingState.CANCELLED, transaction.getCurrentState(),
                "Giữ chỗ thất bại sau khi đã thanh toán phải kích hoạt bù trừ hoàn tiền và chuyển sang CANCELLED");
    }
}
