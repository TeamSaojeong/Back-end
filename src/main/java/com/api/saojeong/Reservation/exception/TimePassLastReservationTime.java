package com.api.saojeong.Reservation.exception;

import com.api.saojeong.global.utill.exception.CustomException;

public class TimePassLastReservationTime extends CustomException {
    public TimePassLastReservationTime() {
        super(ReservationErrorCode.TIME_PASS_LAST_RESERVATION_TIME);
    }
}
