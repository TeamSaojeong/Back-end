package com.api.saojeong.Parking.exception;

import com.api.saojeong.global.utill.exception.CustomException;

public class S3UploadFailedException extends CustomException {
    public S3UploadFailedException() {super(ParkingErrorCode.S3_UPLOAD_FAILED);}
}
