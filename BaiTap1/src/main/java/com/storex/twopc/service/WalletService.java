package com.storex.twopc.service;

import com.storex.twopc.model.BookingData;
import com.storex.twopc.model.PrepareResponse;
import com.storex.twopc.model.VoteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WalletService implements TwoPhaseParticipant {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    // Mock database for customer wallet balances
    private final Map<String, Double> walletBalances = new ConcurrentHashMap<>();
    // Mock database for locked funds per bookingId
    private final Map<String, Double> lockedFunds = new ConcurrentHashMap<>();
    private final Map<String, String> walletStatus = new ConcurrentHashMap<>();

    public WalletService() {
        // Initialize sample wallet balances
        walletBalances.put("W-456", 2000000.0); // 2,000,000 VND
        walletBalances.put("W-100", 500000.0);  // 500,000 VND (Insufficient for 1.2M)
    }

    public void setBalance(String walletId, Double balance) {
        walletBalances.put(walletId, balance);
    }

    public Double getBalance(String walletId) {
        return walletBalances.getOrDefault(walletId, 0.0);
    }

    @Override
    public String getName() {
        return "WalletService";
    }

    @Override
    public synchronized PrepareResponse prepare(BookingData bookingData) {
        log.info("[WalletService] Received PREPARE request for Wallet ID: {}, Amount: {} VND",
                bookingData.getCustomerWalletId(), bookingData.getPrice());

        double currentBalance = walletBalances.getOrDefault(bookingData.getCustomerWalletId(), 0.0);

        if (currentBalance < bookingData.getPrice()) {
            log.warn("[WalletService] PREPARE FAIL: Wallet {} balance ({} VND) is insufficient for required {} VND!",
                    bookingData.getCustomerWalletId(), currentBalance, bookingData.getPrice());
            return new PrepareResponse(getName(), VoteResult.VOTE_ABORT,
                    "Insufficient balance. Current: " + currentBalance + " VND, Required: " + bookingData.getPrice() + " VND");
        }

        // Lock funds
        walletBalances.put(bookingData.getCustomerWalletId(), currentBalance - bookingData.getPrice());
        lockedFunds.put(bookingData.getBookingId(), bookingData.getPrice());
        walletStatus.put(bookingData.getBookingId(), "LOCKED");

        log.info("[WalletService] PREPARE SUCCESS: Locked {} VND for Wallet ID {}. Remaining balance: {} VND",
                bookingData.getPrice(), bookingData.getCustomerWalletId(), walletBalances.get(bookingData.getCustomerWalletId()));

        return new PrepareResponse(getName(), VoteResult.VOTE_COMMIT, "Sufficient balance, funds locked");
    }

    @Override
    public synchronized boolean commit(BookingData bookingData) {
        log.info("[WalletService] Received COMMIT request for Booking ID {}", bookingData.getBookingId());
        String status = walletStatus.get(bookingData.getBookingId());

        if ("LOCKED".equals(status)) {
            walletStatus.put(bookingData.getBookingId(), "COMMITTED");
            lockedFunds.remove(bookingData.getBookingId());
            log.info("[WalletService] COMMIT SUCCESS: Permanently deducted {} VND for Booking ID {}",
                    bookingData.getPrice(), bookingData.getBookingId());
            return true;
        }

        log.warn("[WalletService] COMMIT FAILED: Invalid state {} for Booking ID {}", status, bookingData.getBookingId());
        return false;
    }

    @Override
    public synchronized boolean rollback(BookingData bookingData) {
        log.info("[WalletService] Received ROLLBACK request for Booking ID {}", bookingData.getBookingId());
        String status = walletStatus.get(bookingData.getBookingId());

        if ("LOCKED".equals(status)) {
            double amountToRelease = lockedFunds.getOrDefault(bookingData.getBookingId(), bookingData.getPrice());
            double currentBalance = walletBalances.getOrDefault(bookingData.getCustomerWalletId(), 0.0);

            // Refund locked amount
            walletBalances.put(bookingData.getCustomerWalletId(), currentBalance + amountToRelease);
            lockedFunds.remove(bookingData.getBookingId());
            walletStatus.put(bookingData.getBookingId(), "ROLLED_BACK");

            log.info("[WalletService] ROLLBACK SUCCESS: Refunded {} VND back to Wallet ID {}. Restored balance: {} VND",
                    amountToRelease, bookingData.getCustomerWalletId(), walletBalances.get(bookingData.getCustomerWalletId()));
            return true;
        }

        log.info("[WalletService] ROLLBACK NO-OP: Booking ID {} was not locked.", bookingData.getBookingId());
        return true;
    }

    public String getWalletStatus(String bookingId) {
        return walletStatus.get(bookingId);
    }
}
