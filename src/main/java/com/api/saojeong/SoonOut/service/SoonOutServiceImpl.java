package com.api.saojeong.SoonOut.service;

import com.api.saojeong.Reservation.exception.ReservationNotFound;
import com.api.saojeong.Reservation.repository.ReservationRepository;
import com.api.saojeong.SoonOut.dto.CreateSoonOutRequestDto;
import com.api.saojeong.SoonOut.dto.CreateSoonOutResponseDto;
import com.api.saojeong.SoonOut.respotiory.SoonOutRepository;
import com.api.saojeong.domain.Member;
import com.api.saojeong.domain.Parking;
import com.api.saojeong.domain.Reservation;
import com.api.saojeong.domain.SoonOut;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


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

        //기존 알람이 있는지 확인
        SoonOut soonOut = soonOutRepository.findByReservationIdAndStatus(reservationId, true);

        if(soonOut != null) {
            soonOut.setMinute(req.getSoonMinute());
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
}
