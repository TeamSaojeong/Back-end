package com.api.saojeong.Reservation.repository;

import com.api.saojeong.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByParkingIdAndStatus(Long parkingId, boolean b);

    Optional<Reservation> findFirstByParkingIdAndStatus(Long parkingId, boolean status);

    Optional<Reservation> findByIdAndStatus(Long reservationId, boolean b);
}
