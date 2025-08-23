package com.api.saojeong.alert.repository;

import com.api.saojeong.domain.UserAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;

public interface UserAlertRepository extends JpaRepository<UserAlert, Long> {
    @Query("""
    SELECT ua FROM UserAlert ua
    WHERE ua.member.id = :memberId
      AND (:since IS NULL OR ua.createdAt > :since)
    ORDER BY ua.createdAt DESC
  """)
    List<UserAlert> findNewAlerts(Long memberId, OffsetDateTime since);

    @Modifying
    @Query("UPDATE UserAlert ua SET ua.consumedAt = :ts WHERE ua.id IN :ids AND ua.member.id = :memberId")
    int consumeAlerts(Long memberId, List<Long> ids, OffsetDateTime ts);
}