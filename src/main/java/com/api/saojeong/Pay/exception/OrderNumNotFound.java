package com.api.saojeong.Pay.exception;

public class OrderNumNotFound extends RuntimeException {
    public OrderNumNotFound() {
        super(PayErrorCode.ORDER_NUM_NOT_FOUND.getMessage());
    }
}
