package com.api.saojeong.alert.exception;

import com.api.saojeong.global.utill.exception.CustomException;

public class AlertNotFoundException extends CustomException {
    public AlertNotFoundException() {
        super(AlertErrorCode.ALERT_NOT_FOUND);
    }
}