package com.api.saojeong.Member.service;



import com.api.saojeong.domain.Member;

import java.util.Optional;

public interface MemberQueryService {
    Optional<Member> getMemberWithAuthorities(String loginId);
}
