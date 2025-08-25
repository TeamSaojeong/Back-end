// KakaoCoordinateRefiner.java
package com.api.saojeong.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoCoordinateRefiner implements CoordinateRefiner {

    @Qualifier("kakaoWebClient")
    private final WebClient kakaoWebClient;

    @Value("${kakao.enabled:true}")
    private boolean enabled;

    @Value("${kakao.search.radius-m:1500}")
    private int defaultRadiusM;

    @Value("${kakao.search.limit:5}")
    private int limit;

    @Override
    public Mono<LatLng> refine(String name, double lat, double lon, int searchRadiusM) {
        if (!enabled || name == null || name.isBlank()) {
            return Mono.just(new LatLng(lat, lon, "original"));
        }

        final String q = sanitize(name);
        final int radius = Math.min(5000, Math.max(800, searchRadiusM)); // 0.8~5.0km

        return kakaoWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", q)
                        .queryParam("x", lon) // Kakao: x=lon, y=lat
                        .queryParam("y", lat)
                        .queryParam("radius", radius)
                        .queryParam("category_group_code", "PK6") // 주차장
                        .queryParam("sort", "distance")
                        .queryParam("page", 1)
                        .queryParam("size", Math.min(15, Math.max(1, limit)))
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(KakaoKeywordResponse.class)
                .timeout(Duration.ofMillis(900))
                .map(resp -> {
                    var docs = resp.documents();
                    if (docs == null || docs.isEmpty()) {
                        return new LatLng(lat, lon, "original-nohit");
                    }
                    var d0 = docs.get(0);
                    double plat = Double.parseDouble(d0.y());
                    double plon = Double.parseDouble(d0.x());
                    return new LatLng(plat, plon, "kakao");
                })
                .onErrorResume(e -> {
                    log.debug("[KakaoRefiner] fallback(original) {} => {}", q, e.toString());
                    return Mono.just(new LatLng(lat, lon, "original-error"));
                });
    }

    private static String sanitize(String s) {
        String t = s;
        // 불필요 태그 제거
        t = t.replaceAll("\\((구|시)\\)", "");
        t = t.replaceAll("(공영)?주차장", "주차장");
        // 공백 정리
        t = t.replaceAll("\\s+", " ").trim();
        return t;
    }

    // --- DTOs ---
    public record KakaoKeywordResponse(List<KakaoDoc> documents) {}
    public record KakaoDoc(String id, String place_name, String x, String y,
                           String address_name, String road_address_name) {}
}
