package com.api.saojeong.Parking.exception;

import com.api.saojeong.global.utill.init.ErrorCode;
import com.api.saojeong.global.utill.response.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ParkingErrorCode implements BaseResponseCode {

    S3_UPLOAD_FAILED("S3_UPLOAD_FAILED", 500, "S3 업로드 중 오류가 발생했습니다."),
    PARKING_NOT_FOUND_404("PARKING_NOT_FOUND_404", 404, "주차장을 찾을 수 없습니다."),
    FAIL_GEOCODING("FAIL_GEOCODING",400, "주소를 좌표로 반환할 수 없습니다.");

    private final String code;
    private final int status;
    private final String message;

}
