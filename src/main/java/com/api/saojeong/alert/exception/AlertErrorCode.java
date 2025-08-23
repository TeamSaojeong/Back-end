package com.api.saojeong.alert.exception;

import com.api.saojeong.global.utill.response.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AlertErrorCode implements BaseResponseCode {

    ALERT_NOT_FOUND("ALERT_NOT_FOUND",404,"알림을 찾을 수 없습니다."),
    EVENT_NOT_FOUND("EVENT_NOT_FOUND",404,"해당 이벤트 알림이 존재하지 않습니다.");

    private final String code;
    private final int status;
    private final String message;

}