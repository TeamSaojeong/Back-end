package com.api.saojeong.Reservation.exception;

import com.api.saojeong.global.utill.response.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationErrorCode implements BaseResponseCode {

    NO_OPERATE_TIME("NO_OPERATE_TIME",400 ,"현재 운영시간이 아닙니다."),
    TIME_PASS_LAST_RESERVATION_TIME("TIME_PASS_LAST_RESERVATION_TIME",400 , "현재 운영시간의 마지막 예약 가능 시간을 지났습니다.(운영시간 끝 10분전)"),
    RESERVATION_NOT_FOUND("RESERVATION_NOT_FOUND", 404, "예약을 찾을 수 없습니다.");

    private final String code;
    private final int status;
    private final String message;
}
