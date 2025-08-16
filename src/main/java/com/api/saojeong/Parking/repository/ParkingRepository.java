package com.api.saojeong.Parking.repository;

import com.api.saojeong.Parking.enums.ParkingKind;
import com.api.saojeong.domain.Parking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingRepository extends JpaRepository<Parking,Long> {
    List<Parking> findByMemberIdAndKind(Long member_id, ParkingKind kind);

    Optional<Parking> findByIdAndOperate(Long parkingId, boolean b);
}
