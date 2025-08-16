package com.api.saojeong.Report.exception;

import com.api.saojeong.global.utill.exception.CustomException;

public class MissingCarNumException extends CustomException {
    public MissingCarNumException() {
        super(ReportErrorCode.MISSING_CAR_NUM);
    }
}
