package com.api.saojeong.kakao.csvdata;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
@AllArgsConstructor
@Getter @ToString
public class RateInfo {
    private final String nameRaw;
    private final Integer timeRate; // 분(예: 30)
    private final Integer addRate;  // 원(예: 1000)
    private final Double lat;       // 있으면 사용
    private final Double lon;       // 있으면 사용

}
