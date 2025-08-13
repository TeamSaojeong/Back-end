// KakaoPlace.java
package com.api.saojeong.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoPlace(
        String id,
        String placeName,
        String categoryName,
        String categoryGroupCode,
        String categoryGroupName,
        String phone,
        String addressName,
        String roadAddressName,
        String x,       // 경도 (문자열로 옴)
        String y,       // 위도 (문자열로 옴)
        String placeUrl,
        String distance // m 단위 문자열(정렬/표시에 쓰려면 파싱)
) {}
