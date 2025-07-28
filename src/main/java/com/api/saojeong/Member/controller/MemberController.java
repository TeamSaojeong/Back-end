package com.api.saojeong.Member.controller;



import com.api.saojeong.Member.dto.SignupRequestDto;
import com.api.saojeong.Member.service.MemberService;
import com.api.saojeong.domain.Member;
import com.api.saojeong.global.security.LoginMember;
import com.api.saojeong.global.utill.response.CustomApiResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<CustomApiResponse<?>> signUp(@RequestBody SignupRequestDto dto) {
        ResponseEntity<CustomApiResponse<?>> response = memberService.signUp(dto);
        return response;
    }
    @GetMapping("/mypage")
    public ResponseEntity<CustomApiResponse<?>> getMyPage(@LoginMember Member loginMember) {
        ResponseEntity<CustomApiResponse<?>> response =memberService.getMyPage(loginMember);
        return response;
    }

}
