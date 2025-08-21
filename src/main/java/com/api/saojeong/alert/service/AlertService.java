package com.api.saojeong.alert.service;

import com.api.saojeong.SoonOut.respotiory.SoonOutRepository;
import com.api.saojeong.alert.Enum.NotificationType;
import com.api.saojeong.alert.repository.AlertSubscriptionRepository;
import com.api.saojeong.alert.repository.NotificationEventRepository;
import com.api.saojeong.domain.*;
import com.api.saojeong.memberLocation.repository.MemberLocationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;   // ★ 로그 추가
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j                                       // ★ 로그 추가
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertSubscriptionRepository subRepo;


    @Transactional
    public Long subscribe(Member member, Parking parking,
                          String provider, String externalId,
                          Integer minMinutes, OffsetDateTime expiresAt, boolean active) {
        AlertSubscription sub = AlertSubscription.builder()
                .member(member)
                .parking(parking)
                .provider(provider)
                .externalId(externalId)
                .minMinutes(minMinutes)
                .expiresAt(expiresAt)
                .active(active)
                .build();

        Long id = subRepo.save(sub).getId();
        log.info("[ALERT-SUBSCRIBE] saved id={}, memberId={}, parkingId={}, provider={}, externalId={}, minMinutes={}, expiresAt={}, active={}",
                id,
                member != null ? member.getId() : null,
                parking != null ? parking.getId() : null,
                provider, externalId, minMinutes, expiresAt, active);
        return id;
    }


}
