package com.api.saojeong.Reservation.service;

import com.api.saojeong.Reservation.dto.CreateReservationRequestDto;
import com.api.saojeong.Reservation.dto.CreateReservationResponseDto;
import com.api.saojeong.Reservation.dto.GetReservationResponseDto;
import com.api.saojeong.Reservation.dto.OperateTimeCheck;
import com.api.saojeong.domain.Member;
import com.api.saojeong.domain.Pay;

public interface ReservationService {
    //당일 예약 상태 간단 조회
    GetReservationResponseDto getReservation(Member member, Long parkingId);

    //개인 예약 추가
    CreateReservationResponseDto createReservation(Pay pay);

    //예약시간 연장
    CreateReservationResponseDto extendReservation(Member member, Long reservationId, CreateReservationRequestDto req);

    //민영,공영 추가
    CreateReservationResponseDto createPubPriReservation(Member member, CreateReservationRequestDto requestDto);

    //개인 주차장 예약 상세 조회
    CreateReservationResponseDto getDetailReservation(Member member, long reservationId);

    OperateTimeCheck checkOperateTime(Long id);

    //출차하기
//    CheckOutReservationResponseDto checkoutReservation(Member member, Long reservationId);
}
