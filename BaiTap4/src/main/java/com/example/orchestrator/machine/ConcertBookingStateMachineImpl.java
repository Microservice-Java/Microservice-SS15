package com.example.orchestrator.machine;

import com.example.orchestrator.listener.StateChangeListener;
import com.example.orchestrator.model.*;
import com.example.orchestrator.service.PaymentOrchestrationService;
import com.example.orchestrator.service.ReservationOrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConcertBookingStateMachineImpl implements ConcertBookingStateMachine {

    private static final Logger log = LoggerFactory.getLogger(ConcertBookingStateMachineImpl.class);

    private final PaymentOrchestrationService paymentService;
    private final ReservationOrchestrationService reservationService;
    private final StateChangeListener stateChangeListener;
    private final RetryPolicy retryPolicy;

    // Lưu trạng thái của từng transaction
    private final Map<String, BookingState> transactionStates = new ConcurrentHashMap<>();
    private final Map<String, Boolean> paymentCompletedFlags = new ConcurrentHashMap<>();

    public ConcertBookingStateMachineImpl(PaymentOrchestrationService paymentService,
                                          ReservationOrchestrationService reservationService,
                                          StateChangeListener stateChangeListener,
                                          @Value("${orchestrator.retry.max-attempts:3}") int maxAttempts,
                                          @Value("${orchestrator.retry.delay-ms:100}") long delayMs) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
        this.stateChangeListener = stateChangeListener;
        this.retryPolicy = new RetryPolicy(maxAttempts, delayMs);
    }

    public void setRetryDelayMs(long delayMs) {
        // Option to customize delay for tests if needed
    }

    @Override
    public void process(BookingTransaction transaction) {
        String bookingId = transaction.getBookingId();
        BookingState currentState = transactionStates.getOrDefault(bookingId, transaction.getCurrentState());

        // Update initial state mapping if not present
        if (!transactionStates.containsKey(bookingId)) {
            transactionStates.put(bookingId, currentState);
        }

        // Xác định hành động dựa trên trạng thái hiện tại
        switch (currentState) {
            case INITIATED:
                // Chuyển sang PAYMENT_PENDING và gọi thanh toán
                transition(bookingId, transaction, BookingState.PAYMENT_PENDING, BookingEvent.PROCESS_PAYMENT);
                processPayment(transaction);
                break;

            case PAYMENT_PENDING:
                // Đang ở PAYMENT_PENDING, chờ kết quả từ payment service
                break;

            case PAYMENT_COMPLETED:
                paymentCompletedFlags.put(bookingId, true);
                // Chuyển sang SEAT_RESERVING và gọi giữ chỗ
                transition(bookingId, transaction, BookingState.SEAT_RESERVING, BookingEvent.RESERVE_SEATS);
                processReservation(transaction);
                break;

            case SEAT_RESERVING:
                // Chờ kết quả từ reservation service
                break;

            case BOOKING_CONFIRMED:
                log.info("[Orchestrator] Final State: BOOKING_CONFIRMED for booking {}", bookingId);
                break;

            case CANCELLED:
                log.info("[Orchestrator] Transaction {} is CANCELLED", bookingId);
                break;

            default:
                log.error("Unknown state: {}", currentState);
                break;
        }
    }

    private void transition(String bookingId, BookingTransaction transaction, BookingState newState, BookingEvent event) {
        BookingState oldState = transactionStates.getOrDefault(bookingId, BookingState.INITIATED);
        transactionStates.put(bookingId, newState);
        transaction.setCurrentState(newState);
        stateChangeListener.onStateChanged(bookingId, oldState, newState, event);
    }

    private void processPayment(BookingTransaction transaction) {
        String bookingId = transaction.getBookingId();

        // Thực hiện gọi PaymentService với Retry
        for (int attempt = 1; attempt <= retryPolicy.getMaxAttempts(); attempt++) {
            try {
                log.info("[Orchestrator] RetryPolicy: Activity 'processPayment' - Attempt {}/{}",
                        attempt, retryPolicy.getMaxAttempts());

                boolean success = paymentService.processPayment(transaction);
                if (success) {
                    // Chuyển sang PAYMENT_COMPLETED
                    transition(bookingId, transaction, BookingState.PAYMENT_COMPLETED, BookingEvent.PAYMENT_SUCCESS);
                    // Tiếp tục xử lý (gọi process lại để đi tiếp)
                    process(transaction);
                    return;
                } else {
                    // Payment service returned false
                    transition(bookingId, transaction, BookingState.CANCELLED, BookingEvent.PAYMENT_FAILED);
                    compensationTransaction(transaction);
                    return;
                }
            } catch (Exception e) {
                if (attempt == retryPolicy.getMaxAttempts()) {
                    // Thất bại sau maxAttempts lần -> chuyển sang CANCELLED
                    log.error("[Orchestrator] Payment failed after {} attempts: {}", retryPolicy.getMaxAttempts(), e.getMessage());
                    transition(bookingId, transaction, BookingState.CANCELLED, BookingEvent.PAYMENT_FAILED);
                    compensationTransaction(transaction);
                    return;
                }
                try {
                    Thread.sleep(retryPolicy.getDelayMs());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void processReservation(BookingTransaction transaction) {
        String bookingId = transaction.getBookingId();
        try {
            boolean success = reservationService.reserveSeats(transaction);
            if (success) {
                transition(bookingId, transaction, BookingState.BOOKING_CONFIRMED, BookingEvent.RESERVATION_SUCCESS);
                process(transaction);
            } else {
                transition(bookingId, transaction, BookingState.CANCELLED, BookingEvent.RESERVATION_FAILED);
                compensationTransaction(transaction);
            }
        } catch (Exception e) {
            log.error("[Orchestrator] Reservation exception for booking {}: {}", bookingId, e.getMessage());
            transition(bookingId, transaction, BookingState.CANCELLED, BookingEvent.RESERVATION_FAILED);
            compensationTransaction(transaction);
        }
    }

    private void compensationTransaction(BookingTransaction transaction) {
        log.info("[Orchestrator] Compensation triggered for booking: {}", transaction.getBookingId());

        // Nếu đã hoàn tất thanh toán trước đó nhưng bước sau bị hủy -> gọi hoàn tiền
        if (Boolean.TRUE.equals(paymentCompletedFlags.get(transaction.getBookingId()))) {
            paymentService.refund(transaction);
            paymentCompletedFlags.remove(transaction.getBookingId());
        }
    }
}
