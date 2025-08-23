package com.api.saojeong.alert.exception;

import com.api.saojeong.global.utill.exception.CustomException;

public class EventNotFoundException extends CustomException {
    public EventNotFoundException() {
        super(AlertErrorCode.EVENT_NOT_FOUND);
    }
}
