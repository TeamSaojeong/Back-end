package com.api.saojeong.memberLocation.repository;

import com.api.saojeong.domain.MemberLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberLocationRepository extends JpaRepository<MemberLocation, Long> {
    Optional<MemberLocation> findByMemberId(Long memberId);
    List<MemberLocation> findByMemberIdIn(Collection<Long> memberIds);
}