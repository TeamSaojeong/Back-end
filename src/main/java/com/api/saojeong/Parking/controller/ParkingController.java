package com.api.saojeong.Parking.controller;

import com.api.saojeong.Parking.dto.CreateParkingResponseDto;
import com.api.saojeong.Parking.dto.CreateParkingRequestDto;
import com.api.saojeong.Parking.dto.GetMemberParkingResponseDto;
import com.api.saojeong.Parking.dto.ModifyMemberParkingOperResponseDto;
import com.api.saojeong.Parking.service.memberParkingService;
import com.api.saojeong.domain.Member;
import com.api.saojeong.global.security.LoginMember;
import com.api.saojeong.global.utill.response.CustomApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class ParkingController {

    private final memberParkingService memberParkingService;

    @PostMapping("/parking")
    public ResponseEntity<CustomApiResponse<?>> createMemberParking(@LoginMember Member loginMember,
                                                                    @Valid @RequestPart("request") CreateParkingRequestDto createParkingRequestDto,
                                                                    @RequestPart("image") MultipartFile image){

        CreateParkingResponseDto creatParkingRes = memberParkingService.save(loginMember,image, createParkingRequestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomApiResponse.createSuccess(
                        HttpStatus.CREATED.value(),
                        creatParkingRes ,
                        "주차장 등록 완료"
                ));
    }

    @GetMapping("/parking")
    public ResponseEntity<CustomApiResponse<?>> getMemberParking(@LoginMember Member member){
        List<GetMemberParkingResponseDto> res = memberParkingService.getMemberParking(member);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CustomApiResponse.createSuccess(
                        HttpStatus.OK.value(),
                        res,
                        "개인 주차장 조회 완료"
                ));
    }

    @PatchMapping("/parking/{parking_id}/operate")
    public ResponseEntity<CustomApiResponse<?>> changeOperate(@LoginMember Member member,
                                                              @PathVariable Long parking_id
                                                              ){
        ModifyMemberParkingOperResponseDto res = memberParkingService.modifyMemberParkingOper(member, parking_id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CustomApiResponse.createSuccess(
                        HttpStatus.OK.value(),
                        res,
                        "주차장 활성화 전환 성공"
                ));
    }

}
