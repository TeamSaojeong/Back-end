package com.api.saojeong.global.utill.exception;


import com.api.saojeong.global.utill.init.ErrorCode;
import com.api.saojeong.global.utill.response.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException {
    private final BaseResponseCode errorCode;
}