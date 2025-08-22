package com.api.saojeong.Pay.dto;

import lombok.Data;

@Data
public class OrderRequestDto {
    private String parkName;
    private Long parkingId;
    private int total;
    private int usingMinutes;

    public OrderRequestDto(String 개인, long l, int i, int i1) {
    }
}
