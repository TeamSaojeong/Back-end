package com.api.saojeong.kakao.csvdata;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ParkingRateIndex implements InitializingBean {

    private final ResourceLoader resourceLoader;

    @Value("${rates.csv.path}")
    private String csvPath;

    // nameNorm -> 동일 이름 후보들(좌표가 다를 수 있음)
    private final Map<String, List<RateInfo>> byName = new HashMap<>();

    // CSV 헤더 후보들
    private static final List<String> NAME_HEADERS     = List.of("prkNm","prkplceNm","name","prk_nm");
    // ✅ 오타 수정: addTimeRates → timeRates
    private static final List<String> TIMERATE_HEADERS = List.of("addTimeRates","timerate","time_rate");
    private static final List<String> ADDRATE_HEADERS  = List.of("addRates","addrate","add_rate");
    private static final List<String> LAT_HEADERS      = List.of("lat","latitude","y","LAT");
    private static final List<String> LON_HEADERS      = List.of("lon","lng","longitude","x","LNG");

    @Override
    public void afterPropertiesSet() throws Exception {
        Resource res = resourceLoader.getResource(csvPath);
        if (!res.exists()) {
            throw new IllegalStateException("CSV not found: " + csvPath);
        }

        try (var reader = new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8);
             var parser = CSVParser.parse(reader, CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .setIgnoreSurroundingSpaces(true)
                     // .setDelimiter(';') // 세미콜론 CSV면 이 줄을 활성화
                     .build())) {

            var headerSet = parser.getHeaderMap().keySet();

            String nameH = firstExisting(headerSet, NAME_HEADERS);
            String timeH = firstExisting(headerSet, TIMERATE_HEADERS);
            String addH  = firstExisting(headerSet, ADDRATE_HEADERS);
            String latH  = firstExisting(headerSet, LAT_HEADERS);
            String lonH  = firstExisting(headerSet, LON_HEADERS);

            if (nameH == null || timeH == null || addH == null) {
                throw new IllegalStateException("CSV headers not found (need name/time/add). name="
                        + nameH + " time=" + timeH + " add=" + addH);
            }

            for (CSVRecord r : parser) {
                String nameRaw = trimOrNull(r.get(nameH));
                if (nameRaw == null) continue;

                // 🔑 정규화 키: 공백 제거 + '주차장' 제거
                String key = norm(nameRaw);
                if (key.isEmpty()) continue; // '주차장'만 있는 경우 등은 스킵

                Integer timerate = parseIntSafe(r.get(timeH));
                Integer addrate  = parseIntSafe(r.get(addH));
                Double  lat      = latH != null ? parseDoubleSafe(r.get(latH)) : null;
                Double  lon      = lonH != null ? parseDoubleSafe(r.get(lonH)) : null;

                byName.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(new RateInfo(nameRaw, timerate, addrate, lat, lon));
            }
        }
    }

    public Optional<RateInfo> findBest(String placeName, Double yLat, Double xLon) {
        if (placeName == null) return Optional.empty();
        String key = norm(placeName);
        if (key.isEmpty()) return Optional.empty();

        var list = byName.get(key);
        if (list == null || list.isEmpty()) return Optional.empty();
        if (yLat == null || xLon == null) return Optional.of(list.get(0)); // 좌표 없으면 첫 번째

        // 좌표 있으면 가장 가까운 후보
        return list.stream()
                .min(Comparator.comparingDouble(it -> distanceMeter(yLat, xLon, it.getLat(), it.getLon())));
    }

    private static String firstExisting(Collection<String> headers, List<String> candidates) {
        for (String c : candidates) {
            for (String h : headers) {
                if (h.equalsIgnoreCase(c)) return h;
            }
        }
        return null;
    }

    /**
     * 정규화 규칙:
     * 1) NFKC 정규화 → 소문자
     * 2) '주차장' 제거 (중간 공백 허용: 주 차 장)
     * 3) 모든 공백 제거
     */
    private static String norm(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        // '주차장' 패턴 제거 (중간 공백 허용)
        n = n.replaceAll("주\\s*차\\s*장", "");
        // 모든 공백 제거
        n = n.replaceAll("\\s+", "");
        return n.trim();
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Integer parseIntSafe(String s) {
        if (s == null || s.isBlank()) return null;
        String digits = s.replaceAll("[^0-9-]", "");
        if (digits.isBlank()) return null;
        try { return Integer.parseInt(digits); } catch (Exception e) { return null; }
    }

    private static Double parseDoubleSafe(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }

    // Haversine
    private static double distanceMeter(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return Double.MAX_VALUE;
        double R = 6371e3;
        double phi1 = Math.toRadians(lat1), phi2 = Math.toRadians(lat2);
        double dphi = Math.toRadians(lat2-lat1), dl = Math.toRadians(lon2-lon1);
        double a = Math.sin(dphi/2)*Math.sin(dphi/2)
                + Math.cos(phi1)*Math.cos(phi2)*Math.sin(dl/2)*Math.sin(dl/2);
        return 2*R*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }
}
