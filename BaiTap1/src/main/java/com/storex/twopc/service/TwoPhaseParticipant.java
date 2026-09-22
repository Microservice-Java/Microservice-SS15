package com.storex.twopc.service;

import com.storex.twopc.model.BookingData;
import com.storex.twopc.model.PrepareResponse;

public interface TwoPhaseParticipant {

    String getName();

    PrepareResponse prepare(BookingData bookingData);

    boolean commit(BookingData bookingData);

    boolean rollback(BookingData bookingData);
}
