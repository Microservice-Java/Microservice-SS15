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
public class TrainService implements TwoPhaseParticipant {

    private static final Logger log = LoggerFactory.getLogger(TrainService.class);

    // Mock database for available seats per train
    private final Map<String, Integer> trainSeats = new ConcurrentHashMap<>();
    // Mock database for reservation lock status per bookingId
    private final Map<String, String> bookingStatus = new ConcurrentHashMap<>();

    public TrainService() {
        // Initialize sample train codes
        trainSeats.put("SE5", 10);
        trainSeats.put("SE1", 5);
        trainSeats.put("FULL_TRAIN", 0); // No seats available
    }

    public void setAvailableSeats(String trainCode, int count) {
        trainSeats.put(trainCode, count);
    }

    @Override
    public String getName() {
        return "TrainService";
    }

    @Override
    public synchronized PrepareResponse prepare(BookingData bookingData) {
        log.info("[TrainService] Received PREPARE request for Booking ID: {}, Train Code: {}",
                bookingData.getBookingId(), bookingData.getTrainCode());

        int available = trainSeats.getOrDefault(bookingData.getTrainCode(), 0);

        if (available <= 0) {
            log.warn("[TrainService] PREPARE FAIL: Train {} has no available seats!", bookingData.getTrainCode());
            return new PrepareResponse(getName(), VoteResult.VOTE_ABORT, "Train " + bookingData.getTrainCode() + " is fully booked");
        }

        // Lock seat resource
        bookingStatus.put(bookingData.getBookingId(), "LOCKED");
        log.info("[TrainService] PREPARE SUCCESS: Seat locked for Booking ID {}", bookingData.getBookingId());

        return new PrepareResponse(getName(), VoteResult.VOTE_COMMIT, "Seat available and locked");
    }

    @Override
    public synchronized boolean commit(BookingData bookingData) {
        log.info("[TrainService] Received COMMIT request for Booking ID {}", bookingData.getBookingId());
        String status = bookingStatus.get(bookingData.getBookingId());

        if ("LOCKED".equals(status)) {
            bookingStatus.put(bookingData.getBookingId(), "CONFIRMED");
            int current = trainSeats.getOrDefault(bookingData.getTrainCode(), 0);
            trainSeats.put(bookingData.getTrainCode(), Math.max(0, current - 1));
            log.info("[TrainService] COMMIT SUCCESS: Ticket confirmed for Booking ID {}. Remaining seats for {}: {}",
                    bookingData.getBookingId(), bookingData.getTrainCode(), trainSeats.get(bookingData.getTrainCode()));
            return true;
        }

        log.warn("[TrainService] COMMIT FAILED: Invalid state {} for Booking ID {}", status, bookingData.getBookingId());
        return false;
    }

    @Override
    public synchronized boolean rollback(BookingData bookingData) {
        log.info("[TrainService] Received ROLLBACK request for Booking ID {}", bookingData.getBookingId());
        String status = bookingStatus.get(bookingData.getBookingId());

        if ("LOCKED".equals(status) || "CONFIRMED".equals(status)) {
            bookingStatus.put(bookingData.getBookingId(), "RELEASED");
            log.info("[TrainService] ROLLBACK SUCCESS: Seat lock released for Booking ID {}", bookingData.getBookingId());
            return true;
        }

        log.info("[TrainService] ROLLBACK NO-OP: Booking ID {} was not locked.", bookingData.getBookingId());
        return true;
    }

    public String getBookingStatus(String bookingId) {
        return bookingStatus.get(bookingId);
    }
}
