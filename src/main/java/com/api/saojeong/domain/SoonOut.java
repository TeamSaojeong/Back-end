package com.api.saojeong.domain;

import com.api.saojeong.global.utill.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@Builder
@AllArgsConstructor @NoArgsConstructor
@Table(indexes = {
        @Index(name="idx_soonout_provider_ext", columnList = "provider, external_id"),
        @Index(name="idx_soonout_lat_lng", columnList = "lat, lng")
})
public class SoonOut extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "soonout_id")
    private Long id;

    @Column(name="placeName")
    private String placeName;

    // 위치(외부/내부 공통)
    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lng", nullable = false)
    private Double lng;

    @Column(name="address")
    private String address;

    // 곧나감 남김(활성) 여부 (버튼 누름 여부)
    @Column(name = "status", nullable = false)
    private Boolean status;

    // 10/5 분 같은 남은 시간(분)
    @Column(name = "minute", nullable = false)
    private int minute;

    // 내부 주차장 연계(개인 대여)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_id")
    private Parking parking;

    // 외부 주차장 식별(카카오 등) — parking 이 null일 때 사용
    @Column(name = "provider", length = 32)
    private String provider;       // 예: "KAKAO"

    @Column(name = "external_id", length = 128)
    private String externalId;     // 예: kakao place_id

    // (선택) 예약과의 연결 — 내부 주차 사용 중일 때만
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;
}