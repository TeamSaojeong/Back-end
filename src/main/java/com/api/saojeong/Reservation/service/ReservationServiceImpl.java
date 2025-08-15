package com.api.saojeong.Reservation.service;

import com.api.saojeong.Parking.enums.ParkingKind;
import com.api.saojeong.Parking.exception.ParkingNotFoundException;
import com.api.saojeong.Parking.repository.ParkingRepository;
import com.api.saojeong.Parking.repository.ParkingTimeRepository;
import com.api.saojeong.Reservation.dto.*;
import com.api.saojeong.Reservation.enums.ButtonStatus;
import com.api.saojeong.Reservation.exception.NoOperateTime;
import com.api.saojeong.Reservation.exception.ReservationNotFound;
import com.api.saojeong.Reservation.exception.TimePassLastReservationTime;
import com.api.saojeong.Reservation.repository.ReservationRepository;
import com.api.saojeong.SoonOut.exception.SoonOutNotFound;
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

    //예약 추가
    @Override
    public CreateReservationResponseDto createReservation(Member member, Long parkingId, CreateReservationRequestDto req) {
        //주차장 존재 확인
        Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(ParkingNotFoundException::new);

        //운영시간이 아니거나 마지막 예약 가능 시간을 넘었는지 체크
        OperateTimeCheck check = checkOperateTime(parkingId);
        if(!check.isOperateCheck()) {
            throw new NoOperateTime();
        }
        if(!check.isLastStartCheck()) {
            throw new TimePassLastReservationTime();
        }

        LocalDateTime now = LocalDateTime.now();
        Reservation rev;


        //나중에 결제 api를 만들면 개인 주차장은 결제하기로 결제 후 에약 추가 진행
        //결제를 한 순간이 예약 시간 시작 기준으로 하고 싶은데
        //이러면 예외처리를 하는 순간을 언제로 해야하나..
        //그냥 예약 데이터를 저장하고 결제?

        //개인
        if(parking.getKind() == ParkingKind.PERSONAL){
            //예약이 있는지 확인
            //2개가 있을 수 없는 구조 (-> 버튼에서 확인 후 넘어옴)
            Optional<Reservation> reservation = reservationRepository.findFirstByParkingIdAndStatus(parkingId,true);

            //각 상태에 따라
            //예약이 없으면 바로 생성
            LocalDateTime startTime = now;

            //선 예약 : 곧 나감을 눌렀고
            if (reservation.isPresent()) {

                //곧 나감 활성화&존재 확인
                SoonOut soonOut = soonOutRepository.findByParkingIdAndStatus(parkingId, true )
                        .orElseThrow(SoonOutNotFound::new);

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
            rev = Reservation.builder()
                    .member(member)
                    .parking(parking)
                    .userStart(startTime)
                    .userEnd(startTime.plusMinutes(req.getUsingMinutes()))
                    .status(true)
                    .build();
        }
        else{
            //민영, 공영
            rev = Reservation.builder()
                    .member(member)
                    .parking(parking)
                    .userStart(now)
                    .userEnd(now.plusMinutes(req.getUsingMinutes()))
                    .status(true)
                    .build();
        }

        Reservation res = reservationRepository.save(rev);

        return new CreateReservationResponseDto(
                res.getId(),
                res.getMember().getId(),
                res.getParking().getName(),
                res.getUserStart().toLocalTime(),
                res.getUserEnd().toLocalTime(),
                req.getUsingMinutes());
    }

    //예약 연장
    @Transactional
    @Override
    public CreateReservationResponseDto extendReservation(Member member, Long reservationId, CreateReservationRequestDto req) {

        //활성화된 예약 체크
        Reservation res = reservationRepository.findByIdAndStatus(reservationId, true)
                .orElseThrow(ReservationNotFound::new);

        //예약된 예약자 확인
        if (!res.getMember().getId().equals(res.getId())) {
            throw new IllegalArgumentException("본인의 예약만 연장할 수 있습니다.");
        }

        //예약 시간 추가
        res.setUserEnd(
                res.getUserEnd()
                .plusMinutes(req.getUsingMinutes()));


        return new CreateReservationResponseDto(
                res.getId(),
                res.getMember().getId(),
                res.getParking().getName(),
                res.getUserStart().toLocalTime(),
                res.getUserEnd().toLocalTime(),
                req.getUsingMinutes());
    }

    //출차하기
    @Transactional
    @Override
    public CheckOutReservationResponseDto checkoutReservation(Member member, Long reservationId) {
        //예약 확인
        Reservation reservation = reservationRepository.findByIdAndStatus(reservationId, true)
                .orElseThrow(ReservationNotFound::new);
        //곧나감 알림 확인
        //없으면 사용자가 곧나감을 누르지 않고 출차하는 것
        SoonOut soonOut = soonOutRepository.findByReservationIdAndStatus(reservationId, true);
        boolean soonStatus = false;

        reservation.setStatus(false);
        if(soonOut != null) {
            soonOut.setStatus(false);
            soonStatus = soonOut.getStatus();
        }

        return new CheckOutReservationResponseDto(member.getId(), reservation.getStatus(),soonStatus);
    }


    //운영시간 체크 & 마지막 에약 시작 가능 시각을 넘겼을때 (10분단위)
    private OperateTimeCheck checkOperateTime(Long parkingId) {
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
