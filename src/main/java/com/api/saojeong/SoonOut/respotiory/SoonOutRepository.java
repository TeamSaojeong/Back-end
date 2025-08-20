package com.api.saojeong.SoonOut.respotiory;

import com.api.saojeong.domain.SoonOut;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SoonOutRepository extends JpaRepository<SoonOut,Long> {
    SoonOut findByReservationIdAndStatus(Long reservationId, boolean b);

    Optional<SoonOut> findByParkingIdAndStatus(Long parkingId, boolean b);

    boolean existsByProviderAndExternalId(String provider, String externalId);
    // created_at + minute(분) <= now 인 것들 (상태 true만)
    // created_at + minute(분) <= :now && status=true 인 레코드 배치 조회
    @Modifying
    @Transactional
    @Query(value = """
        DELETE FROM soon_out
        WHERE status = 1
          AND TIMESTAMPADD(MINUTE, minute, created_at) <= :now
        """, nativeQuery = true)
    int deleteExpiredNow(@Param("now") LocalDateTime now);
}
