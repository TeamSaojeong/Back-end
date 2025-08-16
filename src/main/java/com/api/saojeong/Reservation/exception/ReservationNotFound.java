package com.api.saojeong.Reservation.exception;

import com.api.saojeong.global.utill.exception.CustomException;

public class ReservationNotFound extends CustomException {
    public ReservationNotFound() {
        super(ReservationErrorCode.RESERVATION_NOT_FOUND);
    }
}
