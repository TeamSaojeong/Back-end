package com.api.saojeong.Report.exception;

import com.api.saojeong.global.utill.exception.CustomException;

public class MissingImageException extends CustomException {
    public MissingImageException() {
        super(ReportErrorCode.MISSING_IMAGE);
    }
}
