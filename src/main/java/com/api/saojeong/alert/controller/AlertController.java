package com.api.saojeong.alert.controller;



import com.api.saojeong.Member.repository.MemberRepository;
import com.api.saojeong.Parking.repository.ParkingRepository;

import com.api.saojeong.alert.service.AlertService;
import com.api.saojeong.domain.Member;
import com.api.saojeong.domain.Parking;
import com.api.saojeong.global.security.LoginMember;
import com.api.saojeong.global.utill.response.CustomApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final ParkingRepository parkingRepo;
    private final MemberRepository memberRepo;

    @PostMapping
    public ResponseEntity<?> subscribe(@LoginMember Member memberId,
                                       @RequestParam(required = false) Long parkingId,
                                       @RequestParam(required = false) String provider,
                                       @RequestParam(required = false) String externalId,
                                       @RequestParam(required = false) Integer minMinutes,
                                       @RequestParam(required = false)
                                       @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) OffsetDateTime expiresAt,
                                       @RequestParam(defaultValue = "true") boolean active) {
        Member m = memberRepo.findById(memberId.getId()).orElseThrow();
        Parking p = (parkingId != null) ? parkingRepo.findById(parkingId).orElse(null) : null;
        Long id = alertService.subscribe(m, p, provider, externalId, minMinutes, expiresAt, active);
        return ResponseEntity.ok(CustomApiResponse.createSuccess(HttpStatus.OK.value(),
                java.util.Map.of("id", id), "알림 구독 처리 완료"));
    }
}
