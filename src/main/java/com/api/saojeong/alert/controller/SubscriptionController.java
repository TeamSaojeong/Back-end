package com.api.saojeong.alert.controller;

import com.api.saojeong.alert.dto.SubscriptionResponseDto;
import com.api.saojeong.alert.service.SubscriptionService;
import com.api.saojeong.domain.Member;
import com.api.saojeong.global.security.LoginMember;
import com.api.saojeong.global.utill.response.CustomApiResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SubscriptionController {

//    private final SubscriptionService subscriptionService;
//
//    @GetMapping("/sub")
//    public ResponseEntity<CustomApiResponse<?>> getSub(@LoginMember Member member){
//
//        List<SubscriptionResponseDto> res = subscriptionService.getSub(member);
//
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(CustomApiResponse.createSuccess(
//                        HttpStatus.OK.value(),
//                        res,
//                        "구독 리스트 조회 성공"
//                ));
//    }

}
