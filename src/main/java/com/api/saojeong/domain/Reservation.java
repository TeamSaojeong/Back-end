package com.api.saojeong.domain;

import com.api.saojeong.global.utill.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="reservation_id")
    private Long id;

    @Column(name="user_start", nullable = false)
    private LocalDateTime userStart;

    @Column(name="user_end", nullable = false)
    private LocalDateTime userEnd;

    @Column(name="status", nullable = false)
    private Boolean status;

    // 외부 주차장 식별(카카오 등) — parking 이 null일 때 사용
    @Column(name = "provider", length = 32)
    private String provider;       // 예: "KAKAO"

    @Column(name = "external_id", length = 128)
    private String externalId;     // 예: kakao place_id

    //개인
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_id")
    private Parking parking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="pay_id", nullable=false)
    private Pay pay;

}
