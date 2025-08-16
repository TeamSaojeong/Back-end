package com.api.saojeong.SoonOut.controller;

import com.api.saojeong.SoonOut.dto.CreateSoonOutRequestDto;
import com.api.saojeong.SoonOut.dto.CreateSoonOutResponseDto;
import com.api.saojeong.SoonOut.dto.CancelSoonOutResponseDto;
import com.api.saojeong.SoonOut.service.SoonOutService;
import com.api.saojeong.domain.Member;
import com.api.saojeong.global.security.LoginMember;
import com.api.saojeong.global.utill.response.CustomApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class SoonOutController {
    private final SoonOutService soonOutService;

    //곧 알림 추가
    @PostMapping("/reservation/{reservationId}/soon-out")
    public ResponseEntity<CustomApiResponse<?>> createSoonOut(@LoginMember Member member,
                                                      @PathVariable Long reservationId,
                                                      @RequestBody CreateSoonOutRequestDto req) {
        CreateSoonOutResponseDto res = soonOutService.creatSoonOut(member, reservationId, req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomApiResponse.createSuccess(
                        HttpStatus.CREATED.value(),
                        res,
                        "곧 알림이 추가되었습니다."
                ));

    }

    //곧 나감 취소
    @PatchMapping("/soon-out/{soonOutId}")
    public ResponseEntity<CustomApiResponse<?>> cancelSoonOut(@LoginMember Member member,
                                                              @PathVariable Long soonOutId){

        CancelSoonOutResponseDto res = soonOutService.cancelSoonOut(member, soonOutId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CustomApiResponse.createSuccess(
                        HttpStatus.OK.value(),
                        res,
                        "곧 나감이 취소 되었습니다."
                ));
    }

}
