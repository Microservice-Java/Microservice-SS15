package com.storex.twopc;

import com.storex.twopc.coordinator.TwoPhaseCommitCoordinator;
import com.storex.twopc.model.BookingData;
import com.storex.twopc.model.TransactionResult;
import com.storex.twopc.service.TrainService;
import com.storex.twopc.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TwoPhaseCommitTest {

    @Autowired
    private TwoPhaseCommitCoordinator coordinator;

    @Autowired
    private TrainService trainService;

    @Autowired
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        trainService.setAvailableSeats("SE5", 10);
        trainService.setAvailableSeats("FULL_TRAIN", 0);

        walletService.setBalance("W-456", 2000000.0); // 2.0M VND
        walletService.setBalance("W-100", 500000.0);  // 0.5M VND
    }

    @Test
    @DisplayName("Kịch bản 1: Happy Path -> Cả 2 service Ready -> Transaction COMMITTED thành công")
    void testHappyPath_BothServicesReady_TransactionCommitted() {
        // Arrange
        BookingData bookingData = new BookingData("TRAIN-2024-089", "SE5", "W-456", 1200000.0);

        // Act
        TransactionResult result = coordinator.coordinator(bookingData);

        // Assert
        assertEquals(TransactionResult.SUCCESS_COMMITTED, result);
        assertEquals("CONFIRMED", trainService.getBookingStatus("TRAIN-2024-089"));
        assertEquals("COMMITTED", walletService.getWalletStatus("TRAIN-2024-089"));
        assertEquals(800000.0, walletService.getBalance("W-456"), "Số dư ví phải giảm còn 800,000 VND");
    }

    @Test
    @DisplayName("Kịch bản 2: Ví không đủ tiền -> WalletService Abort -> GLOBAL ROLLBACK cho tất cả service")
    void testInsufficientWalletBalance_TriggersGlobalRollback() {
        // Arrange: Wallet W-100 chỉ có 500K, không đủ 1.2M
        BookingData bookingData = new BookingData("TRAIN-2024-090", "SE5", "W-100", 1200000.0);

        // Act
        TransactionResult result = coordinator.coordinator(bookingData);

        // Assert
        assertEquals(TransactionResult.FAILED_ROLLED_BACK, result);

        // TrainService đã khóa ghế ở Phase 1 phải được ROLLBACK nhả ghế ở Phase 2
        assertEquals("RELEASED", trainService.getBookingStatus("TRAIN-2024-090"));
        assertNull(walletService.getWalletStatus("TRAIN-2024-090"));
        assertEquals(500000.0, walletService.getBalance("W-100"), "Số dư ví W-100 phải giữ nguyên 500,000 VND");
    }

    @Test
    @DisplayName("Kịch bản 3: Hết vé tàu -> TrainService Abort -> GLOBAL ROLLBACK cho tất cả service")
    void testNoSeatsAvailable_TriggersGlobalRollback() {
        // Arrange: Tàu FULL_TRAIN đã hết vé
        BookingData bookingData = new BookingData("TRAIN-2024-091", "FULL_TRAIN", "W-456", 1200000.0);

        // Act
        TransactionResult result = coordinator.coordinator(bookingData);

        // Assert
        assertEquals(TransactionResult.FAILED_ROLLED_BACK, result);

        // WalletService đã giữ tiền ở Phase 1 phải được ROLLBACK hoàn lại tiền
        assertEquals("ROLLED_BACK", walletService.getWalletStatus("TRAIN-2024-091"));
        assertEquals(2000000.0, walletService.getBalance("W-456"), "Số dư ví W-456 phải được hoàn trả đủ 2,000,000 VND");
    }
}
