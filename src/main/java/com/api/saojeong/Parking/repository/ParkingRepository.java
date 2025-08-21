package com.api.saojeong.Parking.repository;

import com.api.saojeong.Parking.enums.ParkingKind;
import com.api.saojeong.domain.Parking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingRepository extends JpaRepository<Parking,Long> {
    List<Parking> findByMemberIdAndKind(Long member_id, ParkingKind kind);

    Optional<Parking> findByIdAndOperate(Long parkingId, boolean b);

    @Query("""
      select distinct p
      from Parking p
      left join fetch p.parkingTimes t
      where p.operate = true
        and p.pLat between :minLat and :maxLat
        and p.pLng between :minLng and :maxLng
    """)
    List<Parking> findOperateInBoxWithTimes(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng
    );
}
