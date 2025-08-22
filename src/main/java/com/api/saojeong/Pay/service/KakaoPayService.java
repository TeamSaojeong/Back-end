package com.api.saojeong.Pay.service;

import com.api.saojeong.Pay.dto.KakaoApproveResponseDto;
import com.api.saojeong.Pay.dto.KakaoReadyResponseDto;
import com.api.saojeong.Pay.dto.OrderRequestDto;
import com.api.saojeong.Reservation.dto.CreateReservationResponseDto;
import com.api.saojeong.domain.Member;

public interface KakaoPayService {
    KakaoReadyResponseDto payReady(Member member, OrderRequestDto req);

    CreateReservationResponseDto payApprove(String orderNum, String pgToken);
}
