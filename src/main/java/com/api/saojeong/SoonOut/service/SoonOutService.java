package com.api.saojeong.SoonOut.service;

import com.api.saojeong.SoonOut.dto.CreateSoonOutRequestDto;
import com.api.saojeong.SoonOut.dto.CreateSoonOutResponseDto;
import com.api.saojeong.SoonOut.dto.CancelSoonOutResponseDto;
import com.api.saojeong.domain.Member;

public interface SoonOutService {
    CreateSoonOutResponseDto creatSoonOut(Member member, Long reservationId, CreateSoonOutRequestDto req);
    //곧 나감 알림 취소
    CancelSoonOutResponseDto cancelSoonOut(Member member, Long soonOutId);
}
