package com.api.saojeong.Parking.controller;

import com.api.saojeong.Parking.dto.*;
import com.api.saojeong.Parking.service.MemberParkingService;
import com.api.saojeong.domain.Member;
import com.api.saojeong.global.security.LoginMember;
import com.api.saojeong.global.utill.response.CustomApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class ParkingController {

    private final MemberParkingService memberParkingService;

    //개인 주차장 추가
    @PostMapping("/parking")
    public ResponseEntity<CustomApiResponse<?>> createMemberParking(@LoginMember Member loginMember,
                                                                    @Valid @RequestPart("request") CreateParkingRequestDto createParkingRequestDto,
                                                                    @RequestPart(value = "image", required = true) MultipartFile image){

        CreateParkingResponseDto creatParkingRes = memberParkingService.save(loginMember,image, createParkingRequestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomApiResponse.createSuccess(
                        HttpStatus.CREATED.value(),
                        creatParkingRes ,
                        "주차장 등록 완료"
                ));
    }

    //개인 주차장 단건 조회
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

    //개인 주차장 상세 조회
    @GetMapping("/parking/{parkingId}")
    public ResponseEntity<CustomApiResponse<?>> getMemberParking(@LoginMember Member member,
                                                                 @PathVariable Long parkingId){

        GetDetailMemberParkingResponseDto res = memberParkingService.getDetailMemberParking(member, parkingId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CustomApiResponse.createSuccess(
                        HttpStatus.OK.value(),
                        res,
                        "개인 주차장 상세 조회 성공"
                ));
    }

    //개인 주차장 활성화
    @PatchMapping("/parking/{parkingId}/operate")
    public ResponseEntity<CustomApiResponse<?>> changeOperate(@LoginMember Member member,
                                                              @PathVariable Long parkingId
                                                              ){
        ModifyMemberParkingOperResponseDto res = memberParkingService.modifyMemberParkingOper(member, parkingId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CustomApiResponse.createSuccess(
                        HttpStatus.OK.value(),
                        res,
                        "주차장 활성화 전환 성공"
                ));
    }

    @Transactional
    @PatchMapping("/parking/{parkingId}/modify")
    public ResponseEntity<CustomApiResponse<?>> modifyMemberParking(@LoginMember Member member,
                                                                    @PathVariable Long parkingId,
                                                                    @Valid @RequestPart UpdateMemberParkingRequestDto request,
                                                                    @RequestPart(value = "image", required = false) MultipartFile photo){
        CreateParkingResponseDto res = memberParkingService.updateMemberParking(member, parkingId, request, photo);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CustomApiResponse.createSuccess(
                        HttpStatus.OK.value(),
                        res,
                        "주차장 수정 성공"
                ));
    }

}
