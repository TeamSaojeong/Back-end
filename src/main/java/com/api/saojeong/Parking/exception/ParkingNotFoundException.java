package com.api.saojeong.Parking.exception;

import com.api.saojeong.global.utill.exception.CustomException;

public class ParkingNotFoundException extends CustomException {
    public ParkingNotFoundException() {
        super(ParkingErrorCode.PARKING_NOT_FOUND_404);
    }
}
