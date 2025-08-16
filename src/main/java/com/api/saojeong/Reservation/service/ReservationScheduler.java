package com.api.saojeong.Reservation.service;

import com.api.saojeong.Reservation.repository.ReservationRepository;
import com.api.saojeong.SoonOut.respotiory.SoonOutRepository;
import com.api.saojeong.domain.Reservation;
import com.api.saojeong.domain.SoonOut;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;
    private final SoonOutRepository soonOutRepository;

    @Scheduled(fixedRate = 60000)//1분마다
    public void autoCheckoutReservation() {
        LocalDateTime now = LocalDateTime.now();

        //종료시간 이후 5분이 지났는데도 아직 true 일때
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndUserEndBefore(true, now.minusMinutes(5));

        for(Reservation reservation : expiredReservations){
            reservation.setStatus(false);
            reservationRepository.save(reservation);

            SoonOut soonOut = soonOutRepository.findByReservationIdAndStatus(reservation.getId(), true);
            if (soonOut != null) {
                soonOut.setStatus(false);
                soonOutRepository.save(soonOut);
            }
        }
    }
}
