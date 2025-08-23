package com.api.saojeong.Pay.controller;

import com.api.saojeong.Pay.service.KakaoPayService;
import com.api.saojeong.Reservation.dto.CreateReservationResponseDto;
import com.api.saojeong.Reservation.service.ReservationService;
import com.api.saojeong.domain.Member;
import com.api.saojeong.global.security.LoginMember;
import com.api.saojeong.global.utill.response.CustomApiResponse;
import com.api.saojeong.Pay.dto.KakaoReadyResponseDto;
import com.api.saojeong.Pay.dto.OrderRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pay")
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;
    private final ReservationService reservationService;

    @PostMapping("/ready")
    public ResponseEntity<CustomApiResponse<?>> payReady(@LoginMember Member member,
                                                         @RequestBody OrderRequestDto req) {
        log.info("payReady() 진입. member = {}", member);
        KakaoReadyResponseDto res = kakaoPayService.payReady(member, req);
        log.info("[KAKAO READY] payReady() 완료 - redirectUrl={}", res.getNext_redirect_mobile_url());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CustomApiResponse.createSuccess(
                                HttpStatus.OK.value(),
                                res,
                                "결제 준비 완료"
                        )
                );
    }

    @PostMapping("/approve")
    public ResponseEntity<Void> approve(@RequestParam("pg_token") String pgToken,
                                                        @RequestParam("orderNum") String orderNum) {

        log.info("[카카오 결제 승인] orderNum={}, pg_token={}",orderNum, pgToken);

        CreateReservationResponseDto res = kakaoPayService.payApprove(orderNum, pgToken);
        log.info("[KAKAO APPROVE] 예약 생성 완료 - reservationId={}", res.reservationId());

        //성공시 리다이렉트
        URI redirect = UriComponentsBuilder
                .fromUriString("https://front-end-khaki-one.vercel.app/paycomplete")
                .queryParam("orderId", orderNum)
                .queryParam("reservationId", res.reservationId())
                .build(true).toUri();

        return ResponseEntity
                .status(HttpStatus.FOUND) //302
                .location(redirect)
                .build();
    }

//    @GetMapping("/cancel")
//    public void cancel(){
//        throw new
//    }
//
//    @GetMappint("/fail")
//    public void fail(){
//        throw new B
//    }
}
