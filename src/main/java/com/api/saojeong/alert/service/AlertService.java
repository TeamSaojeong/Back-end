package com.api.saojeong.alert.service;

import com.api.saojeong.SoonOut.respotiory.SoonOutRepository;
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

    private static final double KM_1 = 1000.0;
    private static final double KM_2 = 2000.0;
    private static final Duration LOCATION_TTL = Duration.ofMinutes(15);

    private final AlertSubscriptionRepository subRepo;
    private final NotificationEventRepository eventRepo;
    private final SoonOutRepository soonRepo;
    private final MemberLocationRepository locationRepo;
    private final NotificationService notifier;

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

    @Transactional
    public Long createSoonOut(Double lat, Double lng, int minute, boolean status,
                              Parking parking, String provider, String externalId, Reservation reservation,
                              String placeNameOptional) {

        log.info("[SOONOUT][REQ] lat={}, lng={}, minute={}, status={}, parkingId={}, provider={}, externalId={}, reservationId={}, placeName={}",
                lat, lng, minute, status,
                parking != null ? parking.getId() : null,
                provider, externalId,
                reservation != null ? reservation.getId() : null,
                placeNameOptional);

        SoonOut so = SoonOut.builder()
                .lat(lat).lng(lng)
                .minute(minute)
                .status(status)
                .parking(parking)
                .provider(provider)
                .externalId(externalId)
                .reservation(reservation)
                .build();
        so = soonRepo.save(so);

        log.info("[SOONOUT][SAVED] soonOutId={}, target={}",
                so.getId(),
                parking != null ? ("parkingId=" + parking.getId()) : (provider + ":" + externalId));

        // 대상 구독자 (현재 로직 유지)
        List<AlertSubscription> subs = subRepo.findByProviderAndExternalIdAndActiveIsTrue(provider, externalId);
        log.info("[SOONOUT:{}] subs(provider/external) found={}", so.getId(), subs.size());
        if (subs.isEmpty()) {
            List<AlertSubscription> subs1 = subRepo.findByParkingAndActiveIsTrue(parking);
            log.info("[SOONOUT:{}] subs(parkingId) found={}", so.getId(), subs1 != null ? subs1.size() : null);
            if (subs.isEmpty()) {
                log.info("[SOONOUT:{}] no subscription matches → exit", so.getId());
                return so.getId();
            }
        }

        // 위치 로드
        var memberIds = subs.stream().map(s -> s.getMember().getId()).collect(Collectors.toSet());
        log.info("[SOONOUT:{}] memberIds for location load = {}", so.getId(), memberIds);

        var locs = locationRepo.findByMemberIdIn(memberIds).stream()
                .collect(Collectors.toMap(l -> l.getMember().getId(), l -> l));

        log.info("[SOONOUT:{}] locations loaded = {}", so.getId(), locs.size());

        var now = OffsetDateTime.now(ZoneId.of("Asia/Seoul"));
        int sentCnt = 0;
        int skippedExpired = 0, skippedMin = 0, skippedNoLoc = 0, skippedOldLoc = 0, skippedDistance = 0, skippedDup = 0, skippedNoEmail = 0;

        for (AlertSubscription s : subs) {
            Long memId = s.getMember() != null ? s.getMember().getId() : null;

            if (s.getExpiresAt() != null && s.getExpiresAt().isBefore(now)) {
                skippedExpired++;
                log.info("[SOONOUT:{}][MEM:{}] skip: expired at {}", so.getId(), memId, s.getExpiresAt());
                continue;
            }
            if (s.getMinMinutes() != null && minute < s.getMinMinutes()) {
                skippedMin++;
                log.info("[SOONOUT:{}][MEM:{}] skip: minute {} < minMinutes {}", so.getId(), memId, minute, s.getMinMinutes());
                continue;
            }

            var loc = locs.get(memId);
            if (loc == null) {
                skippedNoLoc++;
                log.info("[SOONOUT:{}][MEM:{}] skip: no location", so.getId(), memId);
                continue;
            }

            // 주의: BaseEntity.updatedAt 타입과 now 타입이 다르면 Duration 계산에서 문제가 될 수 있음 (현 로직 유지)
            try {
                var age = Duration.between(loc.getUpdatedAt(), now).abs();
                if (age.compareTo(LOCATION_TTL) > 0) {
                    skippedOldLoc++;
                    log.info("[SOONOUT:{}][MEM:{}] skip: location too old {} min (TTL={} min)",
                            so.getId(), memId, age.toMinutes(), LOCATION_TTL.toMinutes());
                    continue;
                }
            } catch (Exception e) {
                log.warn("[SOONOUT:{}][MEM:{}] WARN: location age calc failed (updatedAt={}, now={}) → continue",
                        so.getId(), memId, loc.getUpdatedAt(), now, e);
                // 계산 실패 시 그냥 통과시키고 아래 거리 체크 진행
            }

            double dist = haversineMeters(lat, lng, loc.getLat(), loc.getLng());
            boolean shouldSend = false;
            if (dist <= KM_1) {
                shouldSend = (minute == 10 || minute == 5);
            } else if (dist <= KM_2) {
                shouldSend = (minute == 10);
            }
            log.info("[SOONOUT:{}][MEM:{}] dist={}m minute={} shouldSend={}",
                    so.getId(), memId, Math.round(dist), minute, shouldSend);
            if (!shouldSend) {
                skippedDistance++;
                continue;
            }

            var exists = eventRepo.findBySoonOutIdAndMemberId(so.getId(), memId);
            if (exists.isPresent()) {
                skippedDup++;
                log.info("[SOONOUT:{}][MEM:{}] skip: already sent", so.getId(), memId);
                continue;
            }

            String email = s.getMember().getMemberId(); // 현 로직 유지(이 값이 이메일이 아닐 가능성 높음)
            log.info("[SOONOUT:{}][MEM:{}] email(candidate)='{}'", so.getId(), memId, email);

            if (email == null || email.isBlank()) {
                skippedNoEmail++;
                log.info("[SOONOUT:{}][MEM:{}] skip: empty email", so.getId(), memId);
                continue;
            }

            String placeName = (parking != null && parking.getName() != null)
                    ? parking.getName()
                    : (placeNameOptional != null ? placeNameOptional : (provider + ":" + externalId));

            try {
                notifier.sendSoonOutEmail(email, placeName, lat, lng, minute);
                eventRepo.save(NotificationEvent.builder()
                        .soonOutId(so.getId())
                        .member(s.getMember())
                        .sent(true)
                        .build());
                sentCnt++;
                log.info("[SOONOUT:{}][MEM:{}] email SENT to {}", so.getId(), memId, email);
            } catch (Exception e) {
                log.error("[SOONOUT:{}][MEM:{}] email SEND FAILED to {} -> {}", so.getId(), memId, email, e.getMessage(), e);
            }
        }

        log.info("[SOONOUT:{}] summary: sent={}, skippedExpired={}, skippedMin={}, skippedNoLoc={}, skippedOldLoc={}, skippedDistance={}, skippedDup={}, skippedNoEmail={}",
                so.getId(), sentCnt, skippedExpired, skippedMin, skippedNoLoc, skippedOldLoc, skippedDistance, skippedDup, skippedNoEmail);

        return so.getId();
    }

    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }
}
