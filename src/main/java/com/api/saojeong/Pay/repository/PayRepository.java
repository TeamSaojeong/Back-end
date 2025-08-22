package com.api.saojeong.Pay.repository;

import com.api.saojeong.Pay.enums.PayStatus;
import com.api.saojeong.domain.Pay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayRepository extends JpaRepository<Pay,Long> {

    Optional<Pay> findByOrderNumAndStatus(String orderNum, PayStatus payStatus);
}
