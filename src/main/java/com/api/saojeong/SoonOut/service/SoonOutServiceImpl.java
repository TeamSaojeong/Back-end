package com.api.saojeong.SoonOut.service;

import com.api.saojeong.Reservation.exception.ReservationNotFound;
import com.api.saojeong.Reservation.repository.ReservationRepository;
import com.api.saojeong.SoonOut.dto.CreateSoonOutRequestDto;
import com.api.saojeong.SoonOut.dto.CreateSoonOutResponseDto;
import com.api.saojeong.SoonOut.dto.CancelSoonOutResponseDto;
import com.api.saojeong.SoonOut.exception.SoonOutNotFound;
import com.api.saojeong.SoonOut.respotiory.SoonOutRepository;
import com.api.saojeong.domain.Member;
import com.api.saojeong.domain.Parking;
import com.api.saojeong.domain.Reservation;
import com.api.saojeong.domain.SoonOut;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@AllArgsConstructor
public class SoonOutServiceImpl implements SoonOutService {

    private final ReservationRepository reservationRepository;
    private final SoonOutRepository soonOutRepository;

    //곧 알림 추가
    @Override
    public CreateSoonOutResponseDto creatSoonOut(Member member, Long reservationId, CreateSoonOutRequestDto req) {
        //예약 확인
        Reservation reservation = reservationRepository.findByIdAndStatus(reservationId, true)
                .orElseThrow(ReservationNotFound::new);

        Parking parking = reservation.getParking();

        //기존 알람이 있는지 확인(활성화, 비활성화 모두)
        SoonOut soonOut = soonOutRepository.findByReservationIdAndParkingId(reservationId,parking.getId());

        if(soonOut != null) {
            soonOut.setMinute(req.getSoonMinute());
            soonOut.setStatus(true);
        }
        else{
            soonOut = SoonOut.builder()
                    .reservation(reservation)
                    .parking(parking)
                    .lat(parking.getPLat())
                    .lng(parking.getPLng())
                    .status(true)
                    .minute(req.getSoonMinute())
                    .build();
        }

        SoonOut res = soonOutRepository.save(soonOut);

        return new CreateSoonOutResponseDto(res.getId(), res.getParking().getId(),
                res.getReservation().getId(), res.getLat(), res.getLng(), res.getStatus());
    }

    //곧 나감 취소
    @Transactional
    @Override
    public CancelSoonOutResponseDto cancelSoonOut(Member member, Long soonOutId) {
        //곧 나감 존재 확인
        SoonOut soonOut = soonOutRepository.findByIdAndStatus(soonOutId, true)
                .orElseThrow(SoonOutNotFound::new);

        soonOut.setStatus(false);
        return new CancelSoonOutResponseDto(soonOut.getId(), soonOut.getStatus());
    }
}
