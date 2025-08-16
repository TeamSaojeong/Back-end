package com.api.saojeong.Parking.repository;

import com.api.saojeong.Parking.dto.ParkingTimeDto;
import com.api.saojeong.domain.ParkingTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingTimeRepository extends JpaRepository<ParkingTime, Long> {
    List<ParkingTime> findByParkingId(Long parkingId);
}
