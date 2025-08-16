package com.api.saojeong.domain;

import com.api.saojeong.global.utill.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@Table(name="alert_subscription",
        indexes = {
                @Index(name="idx_alert_parking", columnList = "parking_id"),
                @Index(name="idx_alert_provider_ext", columnList = "provider, external_id"),
                @Index(name="idx_alert_member", columnList = "member_id")
        },
        uniqueConstraints = {
                // 중복 구독 방지: 같은 회원이 같은 대상(내부 or 외부)을 중복 구독 못하게
                @UniqueConstraint(name="uq_member_parking", columnNames={"member_id","parking_id"}),
                @UniqueConstraint(name="uq_member_provider_ext", columnNames={"member_id","provider","external_id"})
        })
public class AlertSubscription extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="alert_subscription_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id", nullable = false)
    private Member member;

    // 내부 대상
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="parking_id")
    private Parking parking;

    // 외부 대상
    @Column(name="provider", length = 32)
    private String provider;

    @Column(name="external_id", length = 128)
    private String externalId;

    @Column(name="active", nullable = false)
    private boolean active;

    @Column(name="expires_at")
    private OffsetDateTime expiresAt;

    // (옵션) 최소 남은 시간 조건 등
    @Column(name="min_minutes")
    private Integer minMinutes;
}