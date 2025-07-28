package com.api.saojeong.Member.service;



import com.api.saojeong.Member.dto.SignupRequestDto;
import com.api.saojeong.domain.Member;
import com.api.saojeong.global.utill.response.CustomApiResponse;
import org.springframework.http.ResponseEntity;

public interface MemberService {
    ResponseEntity<CustomApiResponse<?>> signUp(SignupRequestDto dto);

    void updateRefreshToken(Member member, String reIssuedRefreshToken);
    ResponseEntity<CustomApiResponse<?>> getMyPage(Member member);

}
