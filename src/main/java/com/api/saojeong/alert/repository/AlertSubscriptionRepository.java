package com.api.saojeong.alert.repository;


import com.api.saojeong.domain.AlertSubscription;
import com.api.saojeong.domain.Member;
import com.api.saojeong.domain.Parking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface AlertSubscriptionRepository extends JpaRepository<AlertSubscription, Long> {
    List<AlertSubscription> findByParkingAndActiveIsTrue(Parking parking);
    List<AlertSubscription> findByProviderAndExternalIdAndActiveIsTrue(String provider, String externalId);
    List<AlertSubscription> findByMemberAndActiveIsTrue(Member member);
    // ✅ 'IsFalse' 는 상수 조건이므로 파라미터를 받지 않습니다.
    long deleteByActiveIsFalseOrExpiresAtBefore(OffsetDateTime cutoff);
    boolean existsByMember_IdAndProviderAndExternalIdAndActiveIsTrue(Long memberId, String provider, String externalId);
    boolean existsByMember_IdAndParking_IdAndActiveIsTrue(Long memberId, Long parkingId);

}