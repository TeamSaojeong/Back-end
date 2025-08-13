package com.api.saojeong.global.utill.exception;


import com.api.saojeong.global.utill.init.ErrorCode;
import com.api.saojeong.global.utill.response.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class CustomException extends RuntimeException {
    private final Object errorCode;

    // 기존 ErrorCode용 생성자
    public CustomException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public CustomException(BaseResponseCode errorCode) {
        this.errorCode = errorCode;
    }
}