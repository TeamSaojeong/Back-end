package com.api.saojeong.global.utill.exception;


import com.api.saojeong.global.utill.init.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
}