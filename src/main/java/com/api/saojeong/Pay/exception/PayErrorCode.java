package com.api.saojeong.Pay.exception;

import com.api.saojeong.global.utill.response.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayErrorCode implements BaseResponseCode {

    ORDER_NUM_NOT_FOUND("ORDER_NUM_NOT_FOUND",404, "주문 번호를 찾을 수 없습니다.");

    private final String code;
    private final int status;
    private final String message;
}
