package com.api.saojeong.Parking.repository;

import com.api.saojeong.domain.Parking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingRepository extends JpaRepository<Parking,Long> {
}
