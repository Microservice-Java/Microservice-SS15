package com.storex.twopc.coordinator;

import com.storex.twopc.model.*;
import com.storex.twopc.service.TwoPhaseParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TwoPhaseCommitCoordinator {

    private static final Logger log = LoggerFactory.getLogger(TwoPhaseCommitCoordinator.class);

    private final List<TwoPhaseParticipant> participants;

    public TwoPhaseCommitCoordinator(List<TwoPhaseParticipant> participants) {
        this.participants = participants;
    }

    public TransactionResult coordinator(BookingData bookingData) {
        log.info("================================================================================");
        log.info("[COORDINATOR] STARTING 2PC TRANSACTION FOR BOOKING ID: {}", bookingData.getBookingId());
        log.info("[COORDINATOR] Input: {}", bookingData);
        log.info("================================================================================");

        List<PrepareResponse> responses = new ArrayList<>();

        // ---------------------------------------------------------------------
        // PHASE 1: PREPARE PHASE
        // ---------------------------------------------------------------------
        log.info(">>> PHASE 1: PREPARE PHASE - Broadcasting PREPARE request to all participants");
        boolean allReady = true;

        for (TwoPhaseParticipant participant : participants) {
            log.info("[COORDINATOR] Sending PREPARE to [{}]...", participant.getName());
            PrepareResponse response = participant.prepare(bookingData);
            responses.add(response);

            log.info("[COORDINATOR] Received vote from [{}]: {} (Reason: {})",
                    participant.getName(), response.getVote(), response.getReason());

            if (response.getVote() != VoteResult.VOTE_COMMIT) {
                allReady = false;
            }
        }

        // ---------------------------------------------------------------------
        // PHASE 2: DECISION & EXECUTION PHASE
        // ---------------------------------------------------------------------
        if (allReady) {
            log.info("--------------------------------------------------------------------------------");
            log.info(">>> PHASE 2: GLOBAL COMMIT - All participants voted READY (VOTE_COMMIT)");
            log.info("[COORDINATOR] Decision: COMMIT. Broadcasting COMMIT to all participants...");
            log.info("--------------------------------------------------------------------------------");

            for (TwoPhaseParticipant participant : participants) {
                log.info("[COORDINATOR] Sending COMMIT to [{}]...", participant.getName());
                boolean committed = participant.commit(bookingData);
                if (!committed) {
                    log.error("[COORDINATOR] CRITICAL: Participant [{}] failed to commit!", participant.getName());
                }
            }

            log.info("================================================================================");
            log.info("[COORDINATOR] TRANSACTION SUCCESSFUL - Booking {} COMMITTED", bookingData.getBookingId());
            log.info("================================================================================");
            return TransactionResult.SUCCESS_COMMITTED;

        } else {
            log.info("--------------------------------------------------------------------------------");
            log.info(">>> PHASE 2: GLOBAL ROLLBACK - At least one participant voted ABORT (VOTE_ABORT)");
            log.info("[COORDINATOR] Decision: ROLLBACK. Broadcasting ROLLBACK to ALL participants...");
            log.info("--------------------------------------------------------------------------------");

            for (TwoPhaseParticipant participant : participants) {
                log.info("[COORDINATOR] Sending ROLLBACK to [{}]...", participant.getName());
                boolean rolledBack = participant.rollback(bookingData);
                if (!rolledBack) {
                    log.error("[COORDINATOR] CRITICAL: Participant [{}] failed to rollback!", participant.getName());
                }
            }

            log.info("================================================================================");
            log.info("[COORDINATOR] TRANSACTION FAILED - Booking {} ROLLED BACK", bookingData.getBookingId());
            log.info("================================================================================");
            return TransactionResult.FAILED_ROLLED_BACK;
        }
    }
}
