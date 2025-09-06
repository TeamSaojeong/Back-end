package com.api.saojeong.domain;

import com.api.saojeong.Pay.enums.PayStatus;
import com.api.saojeong.global.utill.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pay extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="order_id")
    private Long id;

    @Column(name="order_num", nullable = false)
    private String orderNum;

    @Column(name="total", nullable = false)
    private int total;

    @Column(name="quantity")
    private int quantity;

    @Column(name="tid", nullable = false)
    private String tid;

    @Column(name="using_minutes", nullable = false)
    private int usingMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name="pay_status", nullable = false)
    private PayStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id", nullable = false)
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_id", nullable = false)
    private Parking parking;

}
