package com.api.saojeong.Parking.service;


import com.api.saojeong.Parking.dto.CreateParkingRequestDto;
import com.api.saojeong.Parking.dto.CreateParkingResponseDto;
import com.api.saojeong.Parking.dto.GetMemberParkingResponseDto;
import com.api.saojeong.domain.Member;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface memberParkingService {
    //개인 주차장 추가
    CreateParkingResponseDto save(Member member, MultipartFile image, CreateParkingRequestDto createParkingRequestDto);
    //개인 주차장 리스트
    List<GetMemberParkingResponseDto> getMemberParking(Member member);
}
