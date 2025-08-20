// src/main/java/com/api/saojeong/Reservation/service/HousekeeperScheduler.java
package com.api.saojeong.Reservation.service;

import com.api.saojeong.Reservation.repository.ReservationRepository;
import com.api.saojeong.SoonOut.respotiory.SoonOutRepository;
import com.api.saojeong.alert.Enum.NotificationType;
import com.api.saojeong.alert.repository.NotificationEventRepository;
import com.api.saojeong.alert.service.NotificationService;
import com.api.saojeong.domain.NotificationEvent;
import com.api.saojeong.domain.Reservation;
import com.api.saojeong.domain.SoonOut;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HousekeeperScheduler {

    private final ReservationRepository reservationRepository;
    private final SoonOutRepository soonOutRepository;
    private final NotificationEventRepository eventRepository;
    private final NotificationService notifier;

    private static final int BATCH_LIMIT = 200;
    private static final String EVT_RESERVATION_10M = "RESERVATION_10M";

    @Scheduled(fixedRate = 60_000)
    @SchedulerLock(name = "housekeeper:minute", lockAtMostFor = "PT50S", lockAtLeastFor = "PT10S")
    public void runHousekeeping() {
        ZonedDateTime nowZ = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime now = nowZ.toLocalDateTime();
        log.info("[HK] tick at {}", now);

        try {
            autoCheckoutReservation(now);
        } catch (Exception e) {
            log.error("[HK] autoCheckoutReservation failed: {}", e.getMessage(), e);
        }

        try {
            cleanupSoonOutExpired(now);
        } catch (Exception e) {
            log.error("[HK] cleanupSoonOutExpired failed: {}", e.getMessage(), e);
        }

        try {
            notifyReservationTenMinutes(now);
        } catch (Exception e) {
            log.error("[HK] notifyReservationTenMinutes failed: {}", e.getMessage(), e);
        }
    }

    @Transactional
    protected void autoCheckoutReservation(LocalDateTime now) {
        log.info("[HK][autoCheckoutReservation] start at {}", now);
        List<Reservation> expired = reservationRepository.findByStatusAndUserEndBefore(true, now);
        log.info("[HK][autoCheckoutReservation] expired.size={}", expired.size());
        if (expired.isEmpty()) {
            log.info("[HK][autoCheckoutReservation] no expired reservations");
            return;
        }

        int updated = 0;
        for (Reservation r : expired) {
            r.setStatus(false);
            reservationRepository.save(r);
            updated++;
        }
        log.info("[HK][autoCheckoutReservation] updated={}", updated);
    }
    @Transactional
    protected void cleanupSoonOutExpired(LocalDateTime now) {
        int deleted = soonOutRepository.deleteExpiredNow(now);
        if (deleted > 0) {
                log.info("[HK][cleanupSoonOutExpired] deleted count={}", deleted);
            }
        }

    @Transactional
    protected void notifyReservationTenMinutes(LocalDateTime now) {
        log.info("[HK][notifyReservationTenMinutes] start at {}", now);
        LocalDateTime from = now.plusMinutes(10);
        LocalDateTime to   = now.plusMinutes(11);

        List<Reservation> targets = reservationRepository.findEndInWindow(from, to);
        log.info("[HK][notifyReservationTenMinutes] target.size={} (window {} ~ {})", targets.size(), from, to);
        if (targets.isEmpty()) {
            log.info("[HK][notifyReservationTenMinutes] no target");
            return;
        }

        int sent = 0, skipped = 0;
        for (Reservation r : targets) {
            Long resId = r.getId();
            if (eventRepository.existsByTypeAndReservationId(EVT_RESERVATION_10M, resId)) {
                skipped++;
                continue;
            }

            String email = r.getMember() != null ? r.getMember().getMemberId() : null; // 실제 이메일 컬럼로 교체 권장
            String placeName = (r.getParking() != null) ? r.getParking().getName() : "예약 주차장";
            if (email == null || email.isBlank()) {
                skipped++;
                continue;
            }

            try {
                notifier.sendReservationEndingEmail(email, placeName, 10);
                eventRepository.save(
                        NotificationEvent.builder()
                                .type(NotificationType.RESERVATION_10M)           // ★ NotificationEvent에 type 필드 있는 버전 전제
                                .reservationId(resId)
                                .member(r.getMember())
                                .sent(true)
                                .build()
                );
                sent++;
            } catch (Exception e) {
                log.error("[HK] notify 10m failed for reservationId={}, email={}, err={}", resId, email, e.getMessage(), e);
            }
        }
        log.info("[HK][notifyReservationTenMinutes] sent={} skipped={}", sent, skipped);
    }


}
