package com.api.saojeong.alert.repository;

import com.api.saojeong.alert.Enum.NotificationType;
import com.api.saojeong.domain.Member;
import com.api.saojeong.domain.NotificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {
    Optional<NotificationEvent> findBySoonOutIdAndMemberId(Long soonOutId, Long memberId);
    boolean existsByTypeAndReservationId(NotificationType type, Long reservationId);

    boolean existsByTypeAndSoonOutIdAndMemberId(NotificationType notificationType, Long id, Long memId);

    Optional<NotificationEvent> findBySoonOutIdAndTypeAndMember(Long soonOutId, NotificationType notificationType, Member member);
}