package com.api.saojeong.SoonOut.exception;

import com.api.saojeong.global.utill.response.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SoonOutErrorCode implements BaseResponseCode {

    SOON_OUT_NOT_FOUND("SOON_OUT_NOT_FOUND",400 ,"곧 나가는 차량을 찾지 못하였습니다.");

    private final String code;
    private final int status;
    private final String message;
}
