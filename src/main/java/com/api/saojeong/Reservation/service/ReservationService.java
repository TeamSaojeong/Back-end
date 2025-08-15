package com.api.saojeong.Reservation.service;

import com.api.saojeong.Reservation.dto.CreateReservationRequestDto;
import com.api.saojeong.Reservation.dto.CreateReservationResponseDto;
import com.api.saojeong.Reservation.dto.GetReservationResponseDto;
import com.api.saojeong.domain.Member;

public interface ReservationService {
    //당일 예약 상태 간단 조회
    GetReservationResponseDto getReservation(Member member, Long parkingId);

    //예약 추가
    CreateReservationResponseDto createReservation(Member member, Long parkingId, CreateReservationRequestDto req);

    //예약시간 연장
    CreateReservationResponseDto extendReservation(Member member, Long parkingId, Long reservationId, CreateReservationRequestDto req);
}
