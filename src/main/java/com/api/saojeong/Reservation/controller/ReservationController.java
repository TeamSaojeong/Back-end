package com.api.saojeong.Reservation.controller;

import com.api.saojeong.Reservation.dto.CheckOutReservationResponseDto;
import com.api.saojeong.Reservation.dto.CreateReservationResponseDto;
import com.api.saojeong.Reservation.dto.GetReservationResponseDto;
import com.api.saojeong.Reservation.service.ReservationService;
import com.api.saojeong.Reservation.dto.CreateReservationRequestDto;
import com.api.saojeong.domain.Member;
import com.api.saojeong.global.security.LoginMember;
import com.api.saojeong.global.utill.response.CustomApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
@AllArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    //현재 주차장 예약 상태 간단 조회
    @GetMapping("/parking/{parkingId}/status")
    public ResponseEntity<CustomApiResponse<?>> getReservation(@LoginMember Member member,
                                                               @PathVariable Long parkingId){
        GetReservationResponseDto res = reservationService.getReservation(member, parkingId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CustomApiResponse.createSuccess(
                        HttpStatus.OK.value(),
                        res,
                        "현재 주차장 예약 상태 조회 성공"
                ));
    }

    //예약 시간 추가
    @PostMapping("/parking/{parkingId}/reservation")
    public ResponseEntity<CustomApiResponse<?>> createReservation(@LoginMember Member member,
                                                                  @PathVariable Long parkingId,
                                                                  @RequestBody CreateReservationRequestDto req){

        CreateReservationResponseDto res = reservationService.createReservation(member, parkingId, req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomApiResponse.createSuccess(
                        HttpStatus.CREATED.value(),
                        res,
                        "예약 성공"
                ));
    }

    //연장하기
    @PatchMapping("/reservation/{reservationId}/extend")
    public ResponseEntity<CustomApiResponse<?>> extendReservation(@LoginMember Member member,
                                                                  @PathVariable Long reservationId,
                                                                  @RequestBody CreateReservationRequestDto req){
        CreateReservationResponseDto res = reservationService.extendReservation(member, reservationId, req);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CustomApiResponse.createSuccess(
                        HttpStatus.OK.value(),
                        res,
                        "예약 시간 연장"
                ));
    }

    //출차하기를 눌렀을때
//    @PatchMapping("/reservation/{reservationId}/checkout")
//    public ResponseEntity<CustomApiResponse<?>> checkoutReservation(@LoginMember Member member,
//                                                                    @PathVariable Long reservationId){
//        CheckOutReservationResponseDto res = reservationService.checkoutReservation(member, reservationId);
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(CustomApiResponse.createSuccess(
//                        HttpStatus.OK.value(),
//                        res,
//                        "출차 성공"
//                ));
//    }

}
