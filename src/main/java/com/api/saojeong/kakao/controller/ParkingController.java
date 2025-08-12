package com.api.saojeong.kakao.controller;
import com.api.saojeong.kakao.dto.KakaoSearchResponse;
import com.api.saojeong.kakao.service.ParkingSearchService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parking")
public class ParkingController {
    private final ParkingSearchService service;


    @GetMapping("/nearby")
    public Mono<KakaoSearchResponse> nearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "1000") int radius,   // m
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "distance") String sort
    ) {
        return service.searchNearbyParking(lat, lon, radius, page, size, sort);
    }
}
