package com.api.saojeong.Reservation.repository;

import com.api.saojeong.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByParkingIdAndStatus(Long parkingId, boolean b);

    Optional<Reservation> findFirstByParkingIdAndStatus(Long parkingId, boolean status);

    Optional<Reservation> findByIdAndStatus(Long reservationId, boolean b);

    List<Reservation> findByStatusAndUserEndBefore(boolean b, LocalDateTime now);
    // 기존: 종료시간이 지났는데도 status=true

    // 종료 10분 전 윈도우(1분 폭) & 아직 진행중(status=true)
    @Query("""
      select r
      from Reservation r
      join fetch r.member m
      left join fetch r.parking p
      where r.status = true
        and r.userEnd >= :from and r.userEnd < :to
    """)
    List<Reservation> findEndInWindow(@Param("from") LocalDateTime from,
                                      @Param("to") LocalDateTime to);
}
