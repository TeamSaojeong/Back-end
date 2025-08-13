package com.api.saojeong.kakao.csvdata;




public record ParkingWithRate(
        String id,
        String placeName,
        String addressName,
        Double x,            // 경도
        Double y,            // 위도
        String placeUrl,
        Integer timerate,    // CSV 머지
        Integer addrate,     // CSV 머지
        Integer distance     // (선택) 카카오 distance 파싱 값
) {}