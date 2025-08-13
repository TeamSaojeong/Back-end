package com.api.saojeong.kakao.controller;
import com.api.saojeong.kakao.csvdata.ParkingWithRate;
import com.api.saojeong.kakao.dto.KakaoSearchResponse;
import com.api.saojeong.kakao.service.ParkingSearchService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parking")
public class ParkingController {
    private final ParkingSearchService service;


    @GetMapping(value = "/nearby", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<ParkingWithRate>> nearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "300") int radius,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "distance") String sort
    ) {
        return service.searchNearbyParking(lat, lon, radius, page, size, sort);
    }
}
