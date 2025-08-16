package com.api.saojeong.Report.exception;

import com.api.saojeong.global.utill.response.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportErrorCode implements BaseResponseCode {

    MISSING_CAR_NUM("MISSING_CAR_NUM", 400, "차번호가 입력되지 않았습니다."),
    MISSING_IMAGE("MISSING_IMAGE", 400, "이미지가 등록되지 않았습니다.");

    private final String code;
    private final int status;
    private final String message;
}
