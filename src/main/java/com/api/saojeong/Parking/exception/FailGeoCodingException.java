package com.api.saojeong.Parking.exception;

import com.api.saojeong.global.utill.exception.CustomException;

public class FailGeoCodingException extends CustomException {
    public FailGeoCodingException() {
        super(ParkingErrorCode.FAIL_GEOCODING);
    }
}
