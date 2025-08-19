package com.api.saojeong.SoonOut.controller;


import com.api.saojeong.Parking.repository.ParkingRepository;
import com.api.saojeong.Reservation.repository.ReservationRepository;

import com.api.saojeong.alert.service.AlertService;
import com.api.saojeong.domain.Parking;
import com.api.saojeong.domain.Reservation;
import com.api.saojeong.global.utill.response.CustomApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/soonout")
@RequiredArgsConstructor
public class SoonOutApiController {

    private final AlertService alertService;
    private final ParkingRepository parkingRepo;
    private final ReservationRepository reservationRepo;

    @PostMapping
    public ResponseEntity<?> createSoonOut(@RequestParam double lat,
                                           @RequestParam double lng,
                                           @RequestParam int minute,           // 10 or 5
                                           @RequestParam(defaultValue = "true") boolean status,
                                           @RequestParam(required = false) Long parkingId,     // 내부
                                           @RequestParam(required = false) String provider,    // 외부
                                           @RequestParam(required = false) String externalId,
                                           @RequestParam(required = false) Long reservationId,
                                           @RequestParam(required = false) String placeName,
                                           @RequestParam(required=false)String address) {
        Parking parking = (parkingId != null) ? parkingRepo.findById(parkingId).orElse(null) : null;
        Reservation res = (reservationId != null) ? reservationRepo.findById(reservationId).orElse(null) : null;

        Long id = alertService.createSoonOut(lat, lng, minute, status, parking, provider, externalId, res, placeName,address);
        return ResponseEntity.ok(CustomApiResponse.createSuccess(HttpStatus.OK.value(),
                java.util.Map.of("id", id), "곧나감 등록 및 거리 규칙 기반 알림 발송 처리"));
    }
}
