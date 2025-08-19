package com.api.saojeong.kakao.controller;
import com.api.saojeong.global.utill.response.CustomApiResponse;
import com.api.saojeong.kakao.csvdata.ParkingWithRate;
import com.api.saojeong.kakao.service.ParkingSearchService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parking")
public class ParkingListController {
    private final ParkingSearchService service;


    @GetMapping(value = "/nearby", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<ParkingWithRate>> nearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "1000") int radius,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "distance") String sort
    ) {
        return service.searchNearbyParking(lat, lon, radius, page, size, sort);
    }
    @GetMapping("/detail")
    public Mono<ResponseEntity<CustomApiResponse<ParkingWithRate>>> detail(
            @RequestParam String kakaoId,
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "80") int radius
    ) {
        return service.searchNearbyParking(lat, lon, Math.max(radius, 50), 1, 15, "distance")
                // list -> Mono<ParkingWithRate> (없으면 empty)
                .flatMap(list -> Mono.justOrEmpty(
                        list.stream().filter(p -> kakaoId.equals(p.id())).findFirst()
                ))
                // 있으면 성공 응답
                .map(item -> ResponseEntity.ok(
                        CustomApiResponse.createSuccess(HttpStatus.OK.value(), item, "주차 상세 조회 성공")
                ))
                // 없으면 not found 메시지 (200 + 메시지 유지)
                .switchIfEmpty(Mono.just(
                        ResponseEntity.ok(
                                CustomApiResponse.createSuccessWithoutData(HttpStatus.OK.value(), "해당 ID의 주차장을 찾지 못했습니다.")
                        )
                ));
    }

    @GetMapping("/avg")
    public Mono<ResponseEntity<CustomApiResponse<?>>> avgFeePer10m(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "1000") int radius
    ) {
        return service.searchNearbyParking(lat, lon, radius, 1, 15, "distance")
                .map(list -> {
                    double sum = 0; int n = 0;
                    for (ParkingWithRate p : list) {
                        Integer tr = p.timerate(); // 분
                        Integer ar = p.addrate();  // 원
                        if (tr != null && tr > 0 && ar != null) {
                            // per 10 minutes fee = addrate * (10.0 / timerate)
                            double per10 = ar * (10.0 / tr);
                            sum += per10; n++;
                        }
                    }
                    int avg = (n == 0) ? 0 : (int)Math.round(sum / n);
                    return ResponseEntity.status(HttpStatus.OK)
                            .body(CustomApiResponse.createSuccess(HttpStatus.OK.value(),
                                    java.util.Map.of("averageFeePer10m", avg, "count", n),
                                    "주변 10분당 평균 비용 조회 성공"));
                });
    }
}
