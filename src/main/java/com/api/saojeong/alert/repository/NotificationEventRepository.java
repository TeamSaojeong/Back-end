package com.api.saojeong.alert.repository;

import com.api.saojeong.domain.NotificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {
    Optional<NotificationEvent> findBySoonOutIdAndMemberId(Long soonOutId, Long memberId);
    boolean existsByTypeAndReservationId(String type, Long reservationId);

}