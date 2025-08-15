package com.api.saojeong.Reservation.controller;

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

    //당일 예약 상태 간단 조회
    @GetMapping("/parking/{parking_id}/status")
    public ResponseEntity<CustomApiResponse<?>> getReservation(@LoginMember Member member,
                                                               @PathVariable("parking_id") Long parkingId){
        GetReservationResponseDto res = reservationService.getReservation(member, parkingId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CustomApiResponse.createSuccess(
                        HttpStatus.OK.value(),
                        res,
                        "예약 상태 조회 성공"
                ));
    }

    //예약 시간 추가
    @PostMapping("/parking/{parking_id}/reservation")
    public ResponseEntity<CustomApiResponse<?>> createReservation(@LoginMember Member member,
                                                                  @PathVariable("parking_id") Long parkingId,
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


}
