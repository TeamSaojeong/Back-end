package com.api.saojeong.SoonOut.service;

import com.api.saojeong.SoonOut.dto.CreateSoonOutRequestDto;
import com.api.saojeong.SoonOut.dto.DetailSoonOutEventDto;
import com.api.saojeong.domain.Member;

public interface SoonOutService {
    Long createSoonOut(Member member, CreateSoonOutRequestDto req);

    DetailSoonOutEventDto getSoonOutDetail(Member member, Long soonOutId);

//    CreateSoonOutResponseDto creatSoonOut(Member member, Long reservationId, CreateSoonOutRequestDto req);
    //곧 나감 알림 취소
//    CancelSoonOutResponseDto cancelSoonOut(Member member, Long soonOutId);
}
