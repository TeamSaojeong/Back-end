package com.api.saojeong.kakao.csvdata;


import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public class RateInfo {
    private final String nameRaw;
    private final Integer timerate; // 분(예: 30)
    private final Integer addrate;  // 원(예: 1000)
    private final Double lat;       // 있으면 사용
    private final Double lon;       // 있으면 사용

    public RateInfo(String nameRaw, Integer timerate, Integer addrate, Double lat, Double lon) {
        this.nameRaw = nameRaw;
        this.timerate = timerate;
        this.addrate = addrate;
        this.lat = lat;
        this.lon = lon;
    }
}
