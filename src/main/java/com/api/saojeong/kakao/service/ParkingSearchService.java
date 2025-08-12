package com.api.saojeong.kakao.service;

import com.api.saojeong.kakao.dto.KakaoSearchResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ParkingSearchService {
    private final WebClient kakao;

    public ParkingSearchService(WebClient kakaoWebClient) {
        this.kakao = kakaoWebClient;
    }

    public Mono<KakaoSearchResponse> searchNearbyParking(double lat, double lon, int radiusMeters,
                                                         int page, int size, String sort) {
        // sort: "distance" or "accuracy"
        return kakao.get()
                .uri(uri -> uri
                        .path("/v2/local/search/category.json")
                        .queryParam("category_group_code", "PK6")
                        .queryParam("x", lon)       // 경도
                        .queryParam("y", lat)       // 위도
                        .queryParam("radius", radiusMeters) // 0~20000
                        .queryParam("page", page)   // 1~45
                        .queryParam("size", size)   // 1~15
                        .queryParam("sort", sort)   // distance 권장
                        .build()
                )
                .retrieve()
                .bodyToMono(KakaoSearchResponse.class);
    }
}
