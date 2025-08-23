package com.api.saojeong.SoonOut.service;

import com.api.saojeong.SoonOut.dto.CreateSoonOutRequestDto;
import com.api.saojeong.SoonOut.dto.CreateSoonOutResponseDto;
import com.api.saojeong.SoonOut.dto.CancelSoonOutResponseDto;
import com.api.saojeong.SoonOut.dto.DetailSoonOutEventDto;
import com.api.saojeong.domain.Member;
import com.api.saojeong.domain.Parking;
import com.api.saojeong.domain.Reservation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public interface SoonOutService {
    Long createSoonOut(Double lat, Double lng, int minute, boolean status,
                       Parking parking, String provider, String externalId, Reservation reservation,
                       String placeNameOptional,String address,Member member);

    DetailSoonOutEventDto getSoonOutDetail(Member member, Long soonOutId);

//    CreateSoonOutResponseDto creatSoonOut(Member member, Long reservationId, CreateSoonOutRequestDto req);
    //곧 나감 알림 취소
//    CancelSoonOutResponseDto cancelSoonOut(Member member, Long soonOutId);
}
