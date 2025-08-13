package com.api.saojeong.Parking.repository;

import com.api.saojeong.domain.ParkingTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingTimeRepository extends JpaRepository<ParkingTime, Long> {
}
