package com.api.saojeong.SoonOut.service;

import com.api.saojeong.SoonOut.dto.DetailSoonOutEventDto;
import com.api.saojeong.SoonOut.exception.SoonOutNotFoundException;
import com.api.saojeong.SoonOut.respotiory.SoonOutRepository;
import com.api.saojeong.alert.Enum.NotificationType;
import com.api.saojeong.alert.exception.EventNotFoundException;
import com.api.saojeong.alert.repository.AlertSubscriptionRepository;
import com.api.saojeong.alert.repository.NotificationEventRepository;
import com.api.saojeong.alert.repository.UserAlertRepository;
import com.api.saojeong.alert.service.NotificationService;
import com.api.saojeong.domain.*;
import com.api.saojeong.memberLocation.repository.MemberLocationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SoonOutServiceImpl implements SoonOutService {

    private static final double KM_1 = 1000.0;
    private static final double KM_2 = 2000.0;
    private static final Duration LOCATION_TTL = Duration.ofMinutes(15);

    private final NotificationEventRepository eventRepo;
    private final SoonOutRepository soonRepo;
    private final MemberLocationRepository locationRepo;
    private final NotificationService notifier;
    private final AlertSubscriptionRepository subRepo;
    private final UserAlertRepository userAlertRepository; // 👈 추가 (앞서 만든 Repo)
    private final ObjectMapper objectMapper;

//    private final ReservationRepository reservationRepository;
//    private final SoonOutRepository soonOutRepository;

    //곧나감추가 & 알림 발송
    @Transactional
    public Long createSoonOut(Double lat, Double lng, int minute, boolean status,
                              Parking parking, String provider, String externalId, Reservation reservation,
                              String placeNameOptional,String address,Member member) {

        log.info("[SOONOUT][REQ] lat={}, lng={}, minute={}, status={}, parkingId={}, provider={}, externalId={}, reservationId={}, placeName={}",
                lat, lng, minute, status,
                parking != null ? parking.getId() : null,
                provider, externalId,
                reservation != null ? reservation.getId() : null,
                placeNameOptional);


        SoonOut so ;
        //개인이면
        if(parking !=null){
            so = SoonOut.builder()
                    .lat(lat)
                    .lng(lng)
                    .minute(minute)
                    .status(status)
                    .parking(parking)
                    .reservation(reservation)
                    .placeName(placeNameOptional)
                    .address(address)
                    .build();
        }
        else { //공영,민영

            so = SoonOut.builder()
                    .lat(lat).lng(lng)
                    .minute(minute)
                    .status(status)
                    .provider(provider)
                    .externalId(externalId)
                    .reservation(reservation)
                    .placeName(placeNameOptional)
                    .build();
        }
        so = soonRepo.save(so);



        // 대상 구독자 (현재 로직 유지)
        List<AlertSubscription> subs = Collections.emptyList();
        if (provider != null && !provider.isBlank() && externalId != null && !externalId.isBlank()) {
            subs = subRepo.findByProviderAndExternalIdAndActiveIsTrue(provider, externalId);
        }
        if (subs.isEmpty() && parking != null) {
            subs = subRepo.findByParkingAndActiveIsTrue(parking);
        }

        if (subs.isEmpty()) {
            log.info("[SOONOUT] no subscribers (provider={}, externalId={}, parkingId={})",
                    provider, externalId, parking != null ? parking.getId() : null);
            return so.getId();
        }
        log.info("[SOONOUT] subscribers found={}, provider={}, externalId={}, parkingId={}",
                subs.size(), provider, externalId, parking != null ? parking.getId() : null);

        // 위치 로드
        var memberIds = subs.stream().map(s -> s.getMember().getId()).collect(Collectors.toSet());


        var locs = locationRepo.findByMemberIdIn(memberIds).stream()
                .collect(Collectors.toMap(l -> l.getMember().getId(), l -> l));



        var now = OffsetDateTime.now(ZoneId.of("Asia/Seoul"));
        int sentCnt = 0;
        int skippedSelf=0, skippedExpired = 0, skippedMin = 0, skippedNoLoc = 0, skippedOldLoc = 0, skippedDistance = 0, skippedDup = 0, skippedNoEmail = 0;

        for (AlertSubscription s : subs) {
            Long memId = s.getMember() != null ? s.getMember().getId() : null;

            // (중략) self / 만료 / min / 위치 TTL / 거리 / dup 등 현재 로직 그대로

            //본인 제외
            if (so.getReservation() != null && so.getReservation().getMember() != null
                    && Objects.equals(memId, so.getReservation().getMember().getId())) {
                skippedSelf++; continue;
            }

            //구독 만료시간
            if (s.getExpiresAt() != null && s.getExpiresAt().isBefore(now)) {
                skippedExpired++;

                continue;
            }
            if (s.getMinMinutes() != null && minute < s.getMinMinutes()) {
                skippedMin++;

                continue;
            }

            var loc = locs.get(memId);
            if (loc == null) {
                skippedNoLoc++;
                continue;
            }

            try {
                var age = Duration.between(loc.getUpdatedAt(), now).abs();
                if (age.compareTo(LOCATION_TTL) > 0) {
                    skippedOldLoc++;
                    continue;
                }
            } catch (Exception e) {
                log.warn("[SOONOUT_AlERT] TTL check failed for memberId={}, err={}", memId, e.toString());
                // 계산 실패 시 그냥 통과시키고 아래 거리 체크 진행
            }

            double dist = haversineMeters(lat, lng, loc.getLat(), loc.getLng());
            boolean shouldSend = false;
            if (dist <= KM_1) {
                //거리가  1km 이내일시
                shouldSend = (minute == 10 || minute == 5);
            } else if (dist <= KM_2) {
                //거리가 2km 이내일시
                shouldSend = (minute == 10);
            }

            log.debug("[SOONOUT_AlERT] memId={}, dist={}m, minute={}, shouldSend={}",
                    memId, Math.round(dist), minute, shouldSend);

            //거리애 구독자가 없다면
            if (!shouldSend) {
                skippedDistance++;
                continue;
            }

            boolean dup = eventRepo.existsByTypeAndSoonOutIdAndMemberId(NotificationType.SOONOUT, so.getId(), memId);
            if (dup) { skippedDup++; continue; }

            //이메일 가져오기
            String email = s.getMember().getMemberId(); // 현 로직 유지(이 값이 이메일이 아닐 가능성 높음)

            //이메일이 없을시
            if (email == null || email.isBlank()) {
                skippedNoEmail++;

                continue;
            }

            //장소이름설정 개인 / 민영,공영
            String placeName = (parking != null && parking.getName() != null)
                    ? parking.getName()
                    : placeNameOptional;

//            // 👉👉👉 ▶ INSERT HERE — 프론트용 개인 알림 적재
//            try {
//                userAlertRepository.save(UserAlert.builder()
//                        .member(member)
//                        .type("SOONOUT")
//                        .soonoutId(so.getId())
//                        .title("곧 비어요 (" + minute + "분)")
//                        .body((placeName != null ? placeName : "주차장"))
//                        .createdAt(now)
//                        .build());
//                userAlertRepository.flush();
//            } catch (Exception ex) {
//                log.warn("[SOONOUT_ALERT_FEED] persist failed memId={}, soId={}, err={}", memId, so.getId(), ex.toString());
//                // 저장 실패여도 이메일은 계속 진행
//            }
//            // 👈👈👈 ▶ END INSERT

            try {
                notifier.sendSoonOutEmail(email, placeName, minute, address);

                eventRepo.save(NotificationEvent.builder()
                        .type(NotificationType.SOONOUT)
                        .soonOutId(so.getId())
                        .member(s.getMember())
                        .sent(true)
                        .build());

                sentCnt++;
            } catch (Exception e) {
                log.error("[SOONOUT_AlERT] send/save failed: soId={}, memberId={}, email={}, err={}",
                        so.getId(), memId, email, e.getMessage(), e);
            }
        }

        log.info("[SOONOUT_AlERT] soId={} sent={} skipped: expired={} min={} nolocation={} oldloc={} distance={} dup={} self={} noemail={}",
                so.getId(), sentCnt, skippedExpired, skippedMin, skippedNoLoc, skippedOldLoc,
                skippedDistance, skippedDup, skippedSelf, skippedNoEmail);

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

    //생성된 곧나감 알림 조회
    @Override
    public DetailSoonOutEventDto getSoonOutDetail(Member member, Long soonOutId) {
        //곧나감 확인
        SoonOut soonOut = soonRepo.findByIdAndStatus(soonOutId, true)
                .orElseThrow(SoonOutNotFoundException::new);

        //알림이벤트 확인
        NotificationEvent event = eventRepo.findBySoonOutIdAndTypeAndMember(soonOutId, NotificationType.SOONOUT, member)
                .orElseThrow(EventNotFoundException::new);


        return DetailSoonOutEventDto.builder()
                .soonOutId(soonOutId)
                .notificationType(event.getType())
                .minutes(soonOut.getMinute())
                .parkingName(soonOut.getPlaceName())
                .parkingId(soonOut.getParking() != null ? soonOut.getParking().getId() : null)
                .provider(soonOut.getProvider() != null ? soonOut.getProvider() : null)
                .externalId(soonOut.getExternalId() != null ? soonOut.getExternalId() : null)
                .createdAt(soonOut.getCreatedAt())
                .build();
    }

    //곧 알림 추가
//    @Override
//    public CreateSoonOutResponseDto creatSoonOut(Member member, Long reservationId, CreateSoonOutRequestDto req) {
//        //예약 확인
//        Reservation reservation = reservationRepository.findByIdAndStatus(reservationId, true)
//                .orElseThrow(ReservationNotFound::new);
//
//        Parking parking = reservation.getParking();
//
//        //기존 알람이 있는지 확인(활성화, 비활성화 모두)
//        SoonOut soonOut = soonOutRepository.findByReservationIdAndParkingId(reservationId,parking.getId());
//
//        if(soonOut != null) {
//            soonOut.setMinute(req.getSoonMinute());
//            soonOut.setStatus(true);
//        }
//        else{
//            soonOut = SoonOut.builder()
//                    .reservation(reservation)
//                    .parking(parking)
//                    .lat(parking.getPLat())
//                    .lng(parking.getPLng())
//                    .status(true)
//                    .minute(req.getSoonMinute())
//                    .build();
//        }
//
//        SoonOut res = soonOutRepository.save(soonOut);
//
//        return new CreateSoonOutResponseDto(res.getId(), res.getParking().getId(),
//                res.getReservation().getId(), res.getLat(), res.getLng(), res.getStatus());
//    }

    //곧 나감 취소
//    @Transactional
//    @Override
//    public CancelSoonOutResponseDto cancelSoonOut(Member member, Long soonOutId) {
//        //곧 나감 존재 확인
//        SoonOut soonOut = soonOutRepository.findByIdAndStatus(soonOutId, true)
//                .orElseThrow(SoonOutNotFound::new);
//
//        soonOut.setStatus(false);
//        return new CancelSoonOutResponseDto(soonOut.getId(), soonOut.getStatus());
//    }




}
