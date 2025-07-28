package com.api.saojeong.Member.repository;


import com.api.saojeong.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository  extends JpaRepository<Member, Long> {
    Optional<Member> findByMemberId(String memberId);
    boolean existsByMemberId(String memberId);


    Optional<Member> findByRefreshToken(String refreshToken);


}
