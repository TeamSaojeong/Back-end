package com.api.saojeong.SoonOut.controller;


import com.api.saojeong.Parking.repository.ParkingRepository;
import com.api.saojeong.Reservation.repository.ReservationRepository;

import com.api.saojeong.SoonOut.dto.CreateSoonOutRequestDto;
import com.api.saojeong.SoonOut.service.SoonOutService;
import com.api.saojeong.SoonOut.service.SoonOutServiceImpl;
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

    private final SoonOutService soonOutService;
    private final ParkingRepository parkingRepo;
    private final ReservationRepository reservationRepo;

    @PostMapping
    public ResponseEntity<?> createSoonOut(CreateSoonOutRequestDto req) {
        Parking parking = (req.getParkingId() != null) ? parkingRepo.findById(req.getParkingId()).orElse(null) : null;
        Reservation res = (req.getReservationId()!= null) ? reservationRepo.findById(req.getReservationId()).orElse(null) : null;

        Long id = soonOutService.createSoonOut(req.getLat(), req.getLng(), req.getMinute(), req.isStatus(),
                parking, req.getProvider(), req.getExternalId(),
                res, req.getPlaceName(), req.getAddress());
        return ResponseEntity.ok(CustomApiResponse.createSuccess(HttpStatus.OK.value(),
                java.util.Map.of("id", id), "곧나감 등록 및 거리 규칙 기반 알림 발송 처리"));
    }
}
