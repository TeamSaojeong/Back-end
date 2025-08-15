package com.api.saojeong.SoonOut.respotiory;

import com.api.saojeong.domain.SoonOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SoonOutRepository extends JpaRepository<SoonOut,Long> {
    SoonOut findByReservationIdAndStatus(Long reservationId, boolean b);

    Optional<SoonOut> findByParkingIdAndStatus(Long parkingId, boolean b);

    Optional<SoonOut> findByIdAndStatus(Long soonOutId, boolean b);

    SoonOut findByReservationIdAndParkingId(Long reservationId, Long id);
}
