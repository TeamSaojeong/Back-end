package com.api.saojeong.Member.service;


import com.api.saojeong.Member.repository.MemberRepository;
import com.api.saojeong.domain.Member;
import com.api.saojeong.global.utill.exception.CustomException;
import com.api.saojeong.global.utill.init.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberQueryServiceImp implements MemberQueryService {
    private final MemberRepository memberRepository;
        @Override
        public Optional<Member> getMemberWithAuthorities(String loginId) {

            Member member = memberRepository.findByMemberId(loginId)
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
            System.out.println(Optional.ofNullable(member));
            member.getMemberRoleList().size();
            return Optional.ofNullable(member);
        }

}
