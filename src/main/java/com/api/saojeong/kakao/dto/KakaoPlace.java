package com.api.saojeong.kakao.dto;

public record KakaoPlace(
        String id,
        String place_name,
        String category_name,
        String category_group_code,
        String category_group_name,
        String phone,
        String address_name,
        String road_address_name,
        String x,  // 경도
        String y,  // 위도
        String place_url,
        String distance
) {}