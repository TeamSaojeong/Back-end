// ParkingSearchService.java (컨트롤러 변경 없이 작동)
package com.api.saojeong.kakao.service;

import com.api.saojeong.Parking.repository.ParkingRepository;
import com.api.saojeong.Reservation.dto.OperateTimeCheck;
import com.api.saojeong.domain.Parking;
import com.api.saojeong.domain.ParkingTime;
import com.api.saojeong.kakao.csvdata.ParkingRateIndex;
import com.api.saojeong.kakao.csvdata.ParkingWithRate;
import com.api.saojeong.kakao.csvdata.RateInfo;
import com.api.saojeong.kakao.dto.KakaoSearchResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ParkingSearchService {
    private final WebClient kakaoWebClient;
    private final ParkingRateIndex rateIndex;
    private final ParkingRepository parkingRepository;

    // ✅ 시그니처 그대로 유지 (컨트롤러 수정 불필요)
    public Mono<List<ParkingWithRate>> searchNearbyParking(double lat, double lon, int radiusMeters,
                                                           int page, int size, String sort) {

        // 1) 역지오코딩으로 "가고자 하는 건물명" 자동 추출
        Mono<String> buildingNameMono = resolveBuildingName(lat, lon).defaultIfEmpty("");

        // 2) 카카오 주변 주차장 조회
        //논블로킹
        Mono<List<EnrichedParking>> kakaoMono =
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
                                    "kakao:"+doc.id(),
                                    safe(doc.placeName()),
                                    safe(doc.addressName()),
                                    dx, dy,
                                    safe(doc.placeUrl()),
                                    rate.orElse(null),
                                    dist,
                                    0
                            );
                        }).collect(Collectors.toList()))
                        .onErrorReturn(List.of()); // 외부 API 실패시 빈 리스트


        //3) 우리 DB 개인 주차장 (operate=true + 반경 + 운영시간 포함)
        Mono<List<EnrichedParking>> privateMono = Mono.fromCallable(() -> {
                    //radiusMeters내에 있는 개인 주차장 반환
                    double[] box = bbox(lat, lon, radiusMeters);
                    List<Parking> rows = parkingRepository.findOperateInBoxWithTimes(
                            box[0], box[1], box[2], box[3]);

                    LocalTime now = LocalTime.now(java.time.ZoneId.of("Asia/Seoul"));

                    List<EnrichedParking> list = new ArrayList<>();
                    for (Parking p : rows) {
                        //현재 위치와 개인 주차장 까지의 곡면 거리
                        double d = haversineMeters(lat, lon, p.getPLat(), p.getPLng());
                        if (d > radiusMeters) continue;

                        OperateTimeCheck op = checkOperateTimeNow(p.getParkingTimes(), now);
                        //현재 운영중인지
                        if (!op.isOperateCheck()) continue;
                        //남은 시간이 10분 미만인지
                        if(!op.isLastStartCheck()) continue;

                        //개인 주차장 요금
                        RateInfo rate = new RateInfo(
                                p.getName(),
                                10,
                                p.getCharge(),
                                p.getPLat(),
                                p.getPLng());

                        EnrichedParking e = new EnrichedParking(
                                "db:" + p.getId(),
                                safe(p.getName()),
                                safe(p.getAddress()),
                                p.getPLng(), //x
                                p.getPLat(), //y
                                null,  //placeUrl 없음
                                rate,
                                (int) Math.round(d),
                                op.getRemainTime()
                        );
                        list.add(e);
                    }
                    return list;
                })
                //블로킹
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorReturn(List.of());

        // 4) 점수 계산 → 정렬 → 최종 DTO 매핑
        return Mono.zip(
                        buildingNameMono,
                        kakaoMono.defaultIfEmpty(List.of()),
                        privateMono.defaultIfEmpty(List.of())
                )
                .map(list -> {
                    String buildingName = normalize(list.getT1()); //목적지명
                    List<EnrichedParking> merged = Stream
                            .concat(list.getT2().stream(),  //카카오 리스트
                                    list.getT3().stream())  //개인 주차장 리스트
                            .toList();

                    // 스코어 계산(+개인 주차장 가산)
                    for (EnrichedParking p : merged) {
                        p.score = scoreParking(p, buildingName);
                        if (p.id.startsWith("db:")) {
                            p.score += 1.5; //개인 주차장 약한 가산
                            if (p.limitMinutes != null) {
                                p.score += Math.min(p.limitMinutes, 60) / 60.0; //남은운영시간당(최대 +1.0)
                            }
                        }
                    }


                    return merged.stream()
                            .sorted(Comparator
                                            .comparingDouble((EnrichedParking p) -> -p.score) // 점수 내림차순
                                            .thenComparing(p -> Optional.ofNullable(p.distance).orElse(Integer.MAX_VALUE)) // 동일 점수면 가까운 순
                            )
                            .limit(size)
                            .map(p -> new ParkingWithRate(
                                    p.id, p.placeName, p.addressName,
                                    p.x, p.y, p.placeUrl,
                                    p.rateInfo == null ? null : p.rateInfo.getTimerate(),
                                    p.rateInfo == null ? null : p.rateInfo.getAddrate(),
                                    p.distance
                            ))
                            .collect(Collectors.toList());
                });

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

    //입력한 위도, 경도 중심으로 반경 radiusMeters만큼의 사각형 바운딩 박스의 경계값
    private static double[] bbox(double lat, double lon, int radiusMeters) {
        double dLat = radiusMeters / 111_000.0; //1위도 = 11000m
        double dLng = radiusMeters / (111_000.0 * Math.cos(Math.toRadians(lat)));
        return new double[]{lat - dLat, lat + dLat, lon - dLng, lon + dLng};
    }

    //두 위·경도(현재 위치, 개인 주차장 위치) 좌표 사이의 실제 지표 거리(대권거리), 하버사인 공식으로 거리를 게산
    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0; //지구 평균 반지름
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        //하버사인 공식
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);

        //두 점사이의 곡선 거리(미터)
        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    //운영시간 체크, 종료시간 10분전 체크
    private static OperateTimeCheck checkOperateTimeNow(List<ParkingTime> times, LocalTime now) {
        if (times == null || times.isEmpty())
            return new OperateTimeCheck(false, false, 0);

        for (ParkingTime t : times) {
            LocalTime start = t.getStart();
            LocalTime end   = t.getEnd();

            if (now.isAfter(start) && now.isBefore(end)) {
                LocalTime lastStartTime = end.minusMinutes(10);
                boolean check = now.isBefore(lastStartTime); //마지막 예약 가능 시간 전이면 -> true
                int remainTime = (int) ChronoUnit.MINUTES.between(now, end)+1;

                return new OperateTimeCheck(true, check, Math.max(remainTime, 0));
            }
        }
        return new OperateTimeCheck(false, false, 0);
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
        Integer limitMinutes;

        EnrichedParking(String id, String placeName, String addressName,
                        Double x, Double y, String placeUrl, RateInfo rateInfo, Integer distance, Integer limitMinutes) {
            this.id = id;
            this.placeName = placeName;
            this.addressName = addressName;
            this.x = x; this.y = y;
            this.placeUrl = placeUrl;
            this.rateInfo = rateInfo;
            this.distance = distance;
            this.limitMinutes = limitMinutes;
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
