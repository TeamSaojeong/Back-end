// ParkingSearchService.java (CSV 매칭 포함 버전)
package com.api.saojeong.kakao.service;

import com.api.saojeong.kakao.csvdata.ParkingRateIndex;
import com.api.saojeong.kakao.csvdata.ParkingWithRate;
import com.api.saojeong.kakao.csvdata.RateInfo;
import com.api.saojeong.kakao.dto.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParkingSearchService {
    private final WebClient kakaoWebClient;
    private final ParkingRateIndex rateIndex;

    public Mono<List<ParkingWithRate>> searchNearbyParking(double lat, double lon, int radiusMeters,
                                                           int page, int size, String sort) {
        return kakaoWebClient.get()
                .uri(uri -> uri
                        .path("/v2/local/search/category.json")
                        .queryParam("category_group_code", "PK6")
                        .queryParam("x", lon)        // 경도
                        .queryParam("y", lat)        // 위도
                        .queryParam("radius", radiusMeters)
                        .queryParam("page", page)    // 1~45
                        .queryParam("size", size)    // 1~15
                        .queryParam("sort", sort)    // distance 권장
                        .build())
                .retrieve()
                .bodyToMono(KakaoSearchResponse.class)
                .map(resp -> resp.documents().stream().map(doc -> {
                    // 좌표/거리 파싱
                    Double dx = parseDouble(doc.x());
                    Double dy = parseDouble(doc.y());
                    Integer dist = parseInt(doc.distance());

                    // CSV에서 매칭 (이름 정규화 + 좌표 있으면 가까운 지점 우선)
                    Optional<RateInfo> rate = rateIndex.findBest(doc.placeName(), dy, dx);

                    return new ParkingWithRate(
                            doc.id(),
                            doc.placeName(),
                            doc.addressName(),
                            dx, dy,
                            doc.placeUrl(),
                            rate.map(RateInfo::getTimerate).orElse(null),
                            rate.map(RateInfo::getAddrate).orElse(null),
                            dist
                    );
                }).collect(Collectors.toList()));
    }

    private static Double parseDouble(String s) {
        try { return (s == null || s.isBlank()) ? null : Double.parseDouble(s); }
        catch (Exception e) { return null; }
    }
    private static Integer parseInt(String s) {
        try { return (s == null || s.isBlank()) ? null : Integer.parseInt(s.replaceAll("[^0-9-]","")); }
        catch (Exception e) { return null; }
    }
}
