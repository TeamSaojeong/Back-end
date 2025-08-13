package com.api.saojeong.global.utill.response;

public interface BaseResponseCode {
    String getCode();
    String getMessage();
    int getHttpStatus();
}
