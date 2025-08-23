package com.api.saojeong.Pay.service;

import com.api.saojeong.Parking.exception.ParkingNotFoundException;
import com.api.saojeong.Parking.repository.ParkingRepository;
import com.api.saojeong.Pay.dto.KakaoApproveResponseDto;
import com.api.saojeong.Pay.enums.PayStatus;
import com.api.saojeong.Pay.exception.OrderNumNotFound;
import com.api.saojeong.Pay.repository.PayRepository;
import com.api.saojeong.Reservation.dto.CreateReservationResponseDto;
import com.api.saojeong.Reservation.dto.OperateTimeCheck;
import com.api.saojeong.Reservation.exception.NoOperateTime;
import com.api.saojeong.Reservation.exception.TimePassLastReservationTime;
import com.api.saojeong.Reservation.service.ReservationService;
import com.api.saojeong.domain.Member;
import com.api.saojeong.Pay.dto.KakaoReadyResponseDto;
import com.api.saojeong.Pay.dto.OrderRequestDto;
import com.api.saojeong.domain.Parking;
import com.api.saojeong.domain.Pay;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoPayServiceImpl implements KakaoPayService {

    @Value("${kakaopay.secret-key}")
    private String secretKey;

    @Value("${kakaopay.cid}")
    private String cid;

    private final PayRepository payRepository;
    private final ParkingRepository parkingRepository;
    private final ReservationService reservationService;

    public KakaoReadyResponseDto payReady(Member member, OrderRequestDto req) {

        log.info("[KAKAO READY] 시작 - memberId={}, parkingId={}", member.getId(), req.getParkingId());

        //주차장이 있는지
        Parking parking = parkingRepository.findByIdAndOperate(req.getParkingId(), true)
                .orElseThrow(ParkingNotFoundException::new);

        //운영시간이 아니거나 마지막 예약 가능 시간을 넘었는지 체크
        OperateTimeCheck check = reservationService.checkOperateTime(parking.getId());
        log.info("[KAKAO READY] 운영 시간 체크 결과: {}", check);
        if(!check.isOperateCheck()) {
            throw new NoOperateTime();
        }
        if(!check.isLastStartCheck()) {
            throw new TimePassLastReservationTime();
        }

        log.info("[KAKAO READY] 카카오에 요청 준비 중");

        String orderNum = UUID.randomUUID().toString();

        //헤더
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 바디(타입 주의: 숫자는 숫자, 문자열은 문자열)
        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        params.put("partner_order_id", orderNum);
        params.put("partner_user_id", member.getId().toString());
        params.put("item_name", req.getParkName());
        params.put("quantity", 1);
        params.put("total_amount", req.getTotal());
        params.put("tax_free_amount", 0);

        String base = "https://api.parkhere.store/api/pay";
        //String base = "http://localhost:8080/api/pay";
        params.put("approval_url", base + "/approve?orderNum=" + orderNum);
        params.put("cancel_url",   base + "/cancel?orderNum=" + orderNum);
        params.put("fail_url",     base + "/fail?orderNum=" + orderNum);

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(params, headers);

        RestTemplate rt = new RestTemplate();
        KakaoReadyResponseDto res = rt.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/ready",
                entity,
                KakaoReadyResponseDto.class
        );

        log.info("[KAKAO READY] 요청 완료 - tid={}, redirectUrl={}", res.getTid(), res.getNext_redirect_mobile_url());


        Pay pay = Pay.builder()
                .orderNum(orderNum)
                .member(member)
                .parkingName(req.getParkName())
                .quantity(1)
                .total(req.getTotal())
                .tid(res.getTid())
                .status(PayStatus.READY)
                .parking(parking)
                .usingMinutes(req.getUsingMinutes())
                .build();

        payRepository.save(pay);

        //성공 로그
        log.info("[KAKAO READY] orderId={}, tid={}, nextPCRedirect={}, nextMobileRedirect={}",
                orderNum, res.getTid(), res.getNext_redirect_pc_url(), res.getNext_redirect_mobile_url());

        return res;

    }

    @Transactional
    @Override
    public CreateReservationResponseDto payApprove(String orderNum, String pgToken) {

        Pay pay = payRepository.findByOrderNumAndStatus(orderNum, PayStatus.READY)
                .orElseThrow(OrderNumNotFound::new);

        log.info("[KAKAO APPROVE] 결제 정보 확인 - memberId={}, tid={}", pay.getMember().getId(), pay.getTid());

        //카카오 승인 호출
        //헤더
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> params = new HashMap<>();
        params.put("cid",cid);
        params.put("tid",pay.getTid());
        params.put("partner_order_id", orderNum);
        params.put("partner_user_id", pay.getMember().getId().toString());
        params.put("pg_token", pgToken);

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(params, headers);

        RestTemplate rt = new RestTemplate();
        try {
            KakaoApproveResponseDto kakao = rt.postForObject(
                    "https://open-api.kakaopay.com/online/v1/payment/approve",
                    entity,
                    KakaoApproveResponseDto.class
            );
        }catch (Exception e){
            log.error("[KAKAO APPROVE] 승인 요청 실패", e);
            throw e;
        }


        pay.setStatus(PayStatus.SUCCESS);



        return reservationService.createReservation(pay);
    }
}
