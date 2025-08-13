package com.api.saojeong.Parking.exception;

import com.api.saojeong.global.utill.init.ErrorCode;
import com.api.saojeong.global.utill.response.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ParkingErrorCode implements BaseResponseCode {

    S3_UPLOAD_FAILED("S3_UPLOAD_FAILED", 500, "S3 업로드 중 오류가 발생했습니다.");


    private final String code;
    private final int status;
    private final String message;

}
