// ParkingSearchService.java (컨트롤러 변경 없이 작동)
package com.api.saojeong.kakao.service;

import com.api.saojeong.kakao.csvdata.ParkingRateIndex;
import com.api.saojeong.kakao.csvdata.ParkingWithRate;
import com.api.saojeong.kakao.csvdata.RateInfo;
import com.api.saojeong.kakao.dto.KakaoSearchResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParkingSearchService {
    private final WebClient kakaoWebClient;
    private final ParkingRateIndex rateIndex;

    // ✅ 시그니처 그대로 유지 (컨트롤러 수정 불필요)
    public Mono<List<ParkingWithRate>> searchNearbyParking(double lat, double lon, int radiusMeters,
                                                           int page, int size, String sort) {

        // 1) 역지오코딩으로 "가고자 하는 건물명" 자동 추출
        return resolveBuildingName(lat, lon)
                .defaultIfEmpty("")
                // 2) 주변 주차장 조회
                .flatMap(buildingName ->
                        kakaoWebClient.get()
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
                                // 3) CSV 요금 매칭 + 스코어 계산용 확장 모델로 변환
                                .map(resp -> resp.documents().stream().map(doc -> {
                                    Double dx = parseDouble(doc.x());
                                    Double dy = parseDouble(doc.y());
                                    Integer dist = parseInt(doc.distance());

                                    // CSV에서 요금 매칭 (이름 정규화 + 좌표 가까운 지점 우선)
                                    Optional<RateInfo> rate = rateIndex.findBest(doc.placeName(), dy, dx);

                                    return new EnrichedParking(
                                            doc.id(),
                                            safe(doc.placeName()),
                                            safe(doc.addressName()),
                                            dx, dy,
                                            safe(doc.placeUrl()),
                                            rate.orElse(null),
                                            dist
                                    );
                                }).collect(Collectors.toList()))
                                // 4) 점수 계산 → 정렬 → 최종 DTO 매핑
                                .map(list -> {
                                    final String bnNorm = normalize(buildingName);
                                    list.forEach(p -> p.score = scoreParking(p, bnNorm));
                                    return list.stream()
                                            .sorted(Comparator
                                                    .comparingDouble((EnrichedParking p) -> -p.score) // 점수 내림차순
                                                    .thenComparing(p -> Optional.ofNullable(p.distance).orElse(Integer.MAX_VALUE)) // 동일 점수면 가까운 순
                                            )
                                            .map(p -> new ParkingWithRate(
                                                    p.id, p.placeName, p.addressName,
                                                    p.x, p.y, p.placeUrl,
                                                    p.rateInfo == null ? null : p.rateInfo.getTimerate(),
                                                    p.rateInfo == null ? null : p.rateInfo.getAddrate(),
                                                    p.distance
                                            ))
                                            .collect(Collectors.toList());
                                })
                );
    }

    // -------------------- Scoring -------------------- //
    /**
     * 목표: (1) 해당 건물 자체 주차장 우선, (2) 건물 이용 없이 가능한 공영/공용 우선,
     *      (3) '고객전용/입주자전용' 등 전용 주차장 페널티, (4) 거리 가중, (5) CSV 매칭 보너스
     * categoryName 없이 placeName/addressName만으로 휴리스틱 적용
     */
    private double scoreParking(EnrichedParking p, String buildingNameNorm) {
        String name = normalize(p.placeName);
        String addr = normalize(p.addressName);

        double score = 0.0;

        // 1) 건물 자체 주차장 추정 (건물명 토큰 매칭)
        if (!buildingNameNorm.isBlank()) {
            int tokenHits = tokenMatchCount(buildingNameNorm, name + " " + addr);
            score += tokenHits * 8.0;     // 토큰 일치가 많을수록 크게 가산
            if (name.contains("주차") || addr.contains("주차")) score += 2.0;
        }

        // 2) 공영/공용/지자체 운영 추정 가중치 (이름/주소 키워드)
        if (containsAny(name, "공영", "공용", "시영", "구영", "시청", "구청", "공영주차장")) score += 7.0;
        if (containsAny(addr, "시청", "구청", "공원", "문화센터", "체육관", "국민체육")) score += 1.5;

        // 3) 제한된 주차장(전용) 페널티
        if (containsAny(name, "고객전용", "입주자전용", "방문객전용", "전용주차", "단지내", "세차장전용", "거주자전용")) score -= 7.0;
        if (containsAny(addr, "입주자", "거주자", "전용")) score -= 3.0;
        if (containsAny(name, ".*(점|지점)\\b.*")) score -= 3.0;
        if (containsAny(addr, "브랜치", "매장", "스토어", "shop", "store", "outlet", "아울렛", "센터점")) score -= 3.0;

        // 4) 거리 기반 조정 (가까울수록 가산)
        if (p.distance != null) {
            int d = p.distance;
            if (d <= 500)       score += 3.0 * (1.0 - (d / 500.0));
            else if (d <= 1000) score += 0.5 * (1.0 - ((d - 500) / 500.0));
        }

        // 5) 요금 정보(CSV 매칭) 보너스
        if (p.rateInfo != null) score += 1.0;

        return score;
    }

    // -------------------- Reverse Geocoding -------------------- //

    /** 위/경도 → 건물명 추출 (도로명주소 building_name 우선, 없으면 주소 휴리스틱) */
    private Mono<String> resolveBuildingName(double lat, double lon) {
        return kakaoWebClient.get()
                .uri(uri -> uri
                        .path("/v2/local/geo/coord2address.json")
                        .queryParam("x", lon) // 경도
                        .queryParam("y", lat) // 위도
                        .build())
                .retrieve()
                .bodyToMono(KakaoCoord2AddressResponse.class)
                .map(resp -> {
                    if (resp.documents == null || resp.documents.isEmpty()) return "";
                    var doc = resp.documents.get(0);

                    if (doc.road_address != null && doc.road_address.building_name != null
                            && !doc.road_address.building_name.isBlank()) {
                        return doc.road_address.building_name;
                    }
                    String fallback = doc.road_address != null ? doc.road_address.address_name
                            : (doc.address != null ? doc.address.address_name : "");
                    return extractLikelyBuildingName(fallback);
                });
    }

    // 주소 말미에서 건물명/지번 유사 토큰 추출(간단 휴리스틱)
    private static String extractLikelyBuildingName(String addressName) {
        if (addressName == null || addressName.isBlank()) return "";
        String[] tokens = addressName.split("\\s+");
        for (String t : tokens) {
            if (t.matches(".*(타워|센터|빌딩|스퀘어|몰|아울렛|프라자|플라자|프라임|아파트|오피스텔|상가).*")) {
                return t.replaceAll("[,]$", "");
            }
        }
        if (tokens.length >= 2) return tokens[tokens.length - 2];
        return tokens[tokens.length - 1];
    }

    // -------------------- Utils -------------------- //

    private static String safe(String s) { return s == null ? "" : s; }

    private static Double parseDouble(String s) {
        try { return (s == null || s.isBlank()) ? null : Double.parseDouble(s); }
        catch (Exception e) { return null; }
    }
    private static Integer parseInt(String s) {
        try { return (s == null || s.isBlank()) ? null : Integer.parseInt(s.replaceAll("[^0-9-]","")); }
        catch (Exception e) { return null; }
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.KOREAN).replaceAll("\\s+", " ").trim();
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (k != null && !k.isBlank() && text.contains(k)) return true;
        }
        return false;
    }

    /** buildingName을 공백 기준 토큰화하여 대상 문자열에서 일치 개수 카운트 */
    private static int tokenMatchCount(String buildingNameNorm, String targetNorm) {
        if (buildingNameNorm.isBlank() || targetNorm.isBlank()) return 0;
        String[] tokens = buildingNameNorm.split("\\s+");
        int cnt = 0;
        for (String tk : tokens) {
            if (tk.length() >= 2 && targetNorm.contains(tk)) cnt++;
        }
        return cnt;
    }

    // 점수 저장을 위한 내부 모델 (새 파일 생성 X)
    private static class EnrichedParking {
        final String id;
        final String placeName;
        final String addressName;
        final Double x, y;
        final String placeUrl;
        final RateInfo rateInfo;
        final Integer distance;
        double score;

        EnrichedParking(String id, String placeName, String addressName,
                        Double x, Double y, String placeUrl, RateInfo rateInfo, Integer distance) {
            this.id = id;
            this.placeName = placeName;
            this.addressName = addressName;
            this.x = x; this.y = y;
            this.placeUrl = placeUrl;
            this.rateInfo = rateInfo;
            this.distance = distance;
        }
    }

    // 역지오코딩 응답 파싱용 (private static record, 새 파일 X)
    private static final class KakaoCoord2AddressResponse {
        List<Document> documents;
        static final class Document {
            Address address;
            RoadAddress road_address;
        }
        static final class Address { String address_name; }
        static final class RoadAddress { String building_name; String address_name; }
    }
}
