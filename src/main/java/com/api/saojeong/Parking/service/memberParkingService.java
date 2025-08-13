package com.api.saojeong.Parking.service;


import com.api.saojeong.Parking.dto.CreateParkingRequestDto;
import com.api.saojeong.Parking.dto.CreateParkingResponseDto;
import com.api.saojeong.domain.Member;
import org.springframework.web.multipart.MultipartFile;

public interface memberParkingService {
    //개인 주차장 추가
    CreateParkingResponseDto save(Member member, MultipartFile image, CreateParkingRequestDto createParkingRequestDto);
}
