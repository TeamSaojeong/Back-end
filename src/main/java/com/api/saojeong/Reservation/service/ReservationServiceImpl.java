package com.api.saojeong.Reservation.service;

import com.api.saojeong.Parking.exception.ParkingNotFoundException;
import com.api.saojeong.Parking.repository.ParkingRepository;
import com.api.saojeong.Parking.repository.ParkingTimeRepository;
import com.api.saojeong.Reservation.dto.*;
import com.api.saojeong.Reservation.enums.ButtonStatus;
import com.api.saojeong.Reservation.exception.ReservationNotFound;
import com.api.saojeong.Reservation.repository.ReservationRepository;
import com.api.saojeong.SoonOut.exception.SoonOutNotFoundException;
import com.api.saojeong.SoonOut.respotiory.SoonOutRepository;
import com.api.saojeong.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ParkingTimeRepository parkingTimeRepository;
    private final ParkingRepository parkingRepository;
    private final SoonOutRepository soonOutRepository;

    //당일 예약 상태 간단 조회
    @Override
    public GetReservationResponseDto getReservation(Member member, Long parkingId) {

        //운영시간 체크
        OperateTimeCheck check = checkOperateTime(parkingId);
        //운영시간이 아니거나 마지막 예약 가능 시간을 지나면
        if(!check.isOperateCheck() || !check.isLastStartCheck()) {
            return new GetReservationResponseDto(ButtonStatus.UNAVAILABLE, 0, 0);
        }

        //바로 예약 가능할때
        ButtonStatus buttonStatus = ButtonStatus.AVAILABLE;
        int soonOutTime = 0;
        int remainTime = check.getRemainTime();

        //예약 확인
        List<Reservation> reservations = reservationRepository.findByParkingIdAndStatus(parkingId, true);

        if(!reservations.isEmpty()){
            //선예약
            if(reservations.size() == 1){
                //지금 예약이 1개와 곧나감 활성화된게 있고
                Optional<SoonOut> soonOutOpt = soonOutRepository.findByParkingIdAndStatus(parkingId, true );

                //존재하지 않으면 -> Using
                if(soonOutOpt.isPresent()){
                    SoonOut soonOut = soonOutOpt.get();
                    buttonStatus = ButtonStatus.RESERVABLE;
                    soonOutTime = soonOut.getMinute();

                    //남은 시간
                    //10~6분 남았을때 : 현재시간에서 10분 뒤부터 시작가능
                    //5~1분 남았을때 : 현재시간에서 5분 뒤부터 시작
                    remainTime -= soonOutTime;

                }
                else
                    buttonStatus = ButtonStatus.USING;
            }
            //그외 모든 것
            //누군가 곧나감을 누르고 사용중에 다른사람이 다음 선예약을 했을때
            else{
                buttonStatus = ButtonStatus.USING;
            }
        }
        return new GetReservationResponseDto(buttonStatus, soonOutTime, remainTime);
    }



    //개인 예약 추가
    @Override
    public CreateReservationResponseDto createReservation(Pay pay) {

        //주차장 존재 확인
        Parking parking = parkingRepository.findByIdAndOperate(pay.getParking().getId(), true)
                .orElseThrow(ParkingNotFoundException::new);

        //=> 결제 하기전에 확인 하는 것으로 수정
//        //운영시간이 아니거나 마지막 예약 가능 시간을 넘었는지 체크
//        OperateTimeCheck check = checkOperateTime(parking.getId());
//        if(!check.isOperateCheck()) {
//            throw new NoOperateTime();
//        }
//        if(!check.isLastStartCheck()) {
//            throw new TimePassLastReservationTime();
//        }

        LocalDateTime now = LocalDateTime.now();

        //개인
        //예약이 있는지 확인
        //2개가 있을 수 없는 구조 (-> 버튼에서 확인 후 넘어옴)
        Optional<Reservation> reservation = reservationRepository.findFirstByParkingIdAndStatus(parking.getId(),true);
        //각 상태에 따라
        //예약이 없으면 바로 생성
        LocalDateTime startTime = now;

        //선 예약 : 곧 나감을 눌렀고
        if (reservation.isPresent()) {

            //곧 나감 활성화&존재 확인
            SoonOut soonOut = soonOutRepository.findByParkingIdAndStatus(parking.getId(), true )
                    .orElseThrow(SoonOutNotFoundException::new);

            //10~6분 남았을때 : 예약하는 시간 +10분 뒤부터 시작
            if(soonOut.getMinute() == 10){
                startTime = now.plusMinutes(10);
            }
            //5~1분 남았을때 : 예약하는 시간 +5분 뒤부터 시작
            else if(soonOut.getMinute() == 5){
                startTime = now.plusMinutes(5);
            }

        }
        //생성
        Reservation rev = Reservation.builder()
                .member(pay.getMember())
                .parking(parking)
                .userStart(startTime)
                .userEnd(startTime.plusMinutes(pay.getUsingMinutes()))
                .status(true)
                .pay(pay)
                .build();


        Reservation res = reservationRepository.save(rev);

        return new CreateReservationResponseDto(
                res.getId(),
                res.getMember().getId(),
                res.getParking().getName(),
                res.getUserStart().toLocalTime(),
                res.getUserEnd().toLocalTime(),
                pay.getUsingMinutes());
    }



    //공영, 민영 예약 추가
    @Override
    public CreateReservationResponseDto createPubPriReservation(Member member,CreateReservationRequestDto req) {

        //카카오 확인

        LocalDateTime now = LocalDateTime.now();

        Reservation reservation = Reservation.builder()
                .member(member)
                .externalId(req.getExternalId())
                .provider(req.getProvider())
                .userStart(now)
                .userEnd(now.plusMinutes(req.getUsingMinutes()))
                .status(true)
                .build();

        Reservation res = reservationRepository.save(reservation);

        return new CreateReservationResponseDto(
                res.getId(),
                res.getMember().getId(),
                req.getPlacename(),
                res.getUserStart().toLocalTime(),
                res.getUserEnd().toLocalTime(),
                req.getUsingMinutes());
    }



    //개인 결제 내용 조회
    @Override
    public CreateReservationResponseDto getDetailReservation(Member member, long reservationId) {

        Reservation res = reservationRepository.findByIdAndStatus(reservationId, true)
                .orElseThrow(ReservationNotFound::new);



        return new CreateReservationResponseDto(
                res.getId(),
                res.getMember().getId(),
                res.getParking().getName(),
                res.getUserStart().toLocalTime(),
                res.getUserEnd().toLocalTime(),
                res.getPay().getUsingMinutes());
    }



    //예약 연장
    @Transactional
    @Override
    public CreateReservationResponseDto extendReservation(Member member, Long reservationId, CreateReservationRequestDto req) {

        //활성화된 예약 체크
        Reservation res = reservationRepository.findByIdAndStatus(reservationId, true)
                .orElseThrow(ReservationNotFound::new);

        //예약된 예약자 확인
        if (!res.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("본인의 예약만 연장할 수 있습니다.");
        }

        //예약 시간 추가
        res.setUserEnd(
                res.getUserEnd()
                .plusMinutes(req.getUsingMinutes()));

        //곧나감이 존재하면 비활성화
        SoonOut soonOut = soonOutRepository.findByReservationIdAndStatus(reservationId, true);
        if(soonOut != null){
            soonOut.setStatus(false);
        }


        return new CreateReservationResponseDto(
                res.getId(),
                res.getMember().getId(),
                req.getPlacename(),
                res.getUserStart().toLocalTime(),
                res.getUserEnd().toLocalTime(),
                req.getUsingMinutes());
    }



    //출차하기
//    @Transactional
//    @Override
//    public CheckOutReservationResponseDto checkoutReservation(Member member, Long reservationId) {
//        //예약 확인
//        Reservation reservation = reservationRepository.findByIdAndStatus(reservationId, true)
//                .orElseThrow(ReservationNotFound::new);
//        //곧나감 알림 확인
//        //없으면 사용자가 곧나감을 누르지 않고 출차하는 것
//        SoonOut soonOut = soonOutRepository.findByReservationIdAndStatus(reservationId, true);
//        boolean soonStatus = false;
//
//        reservation.setStatus(false);
//        if(soonOut != null) {
//            soonOut.setStatus(false);
//            soonStatus = soonOut.getStatus();
//        }
//
//        return new CheckOutReservationResponseDto(member.getId(), reservation.getStatus(),soonStatus);
//    }


    //운영시간 체크 & 마지막 에약 시작 가능 시각을 넘겼을때 (10분단위)
    public OperateTimeCheck checkOperateTime(Long parkingId) {
        LocalTime now = LocalTime.now();

        //운영시간
        List<ParkingTime> operateTimes = parkingTimeRepository.findByParkingId(parkingId);

        for (ParkingTime parkingTime : operateTimes) {
            LocalTime start = parkingTime.getStart();
            LocalTime end = parkingTime.getEnd();

            //현재 시간이 포함된 운영시간 찾기
            if (now.isAfter(start) && now.isBefore(end)) {
                LocalTime lastStartTime = end.minusMinutes(10);
                boolean check = now.isBefore(lastStartTime); //마지막 예약 가능 시간 전이면 -> true
                int remainTime = (int) ChronoUnit.MINUTES.between(now, end)+1;
                System.out.println("now : "+now+"end"+end+"="+remainTime);
                return new OperateTimeCheck(true, check, remainTime);
            }
        }
        //운영시간 x
        return new OperateTimeCheck(false, false, 0);
    }


}
