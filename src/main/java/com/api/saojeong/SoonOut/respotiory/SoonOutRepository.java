package com.api.saojeong.SoonOut.respotiory;

import com.api.saojeong.domain.SoonOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SoonOutRepository extends JpaRepository<SoonOut,Long> {
    Optional<SoonOut> findByParkingIdAndStatus(Long parkingId, boolean b);
}
