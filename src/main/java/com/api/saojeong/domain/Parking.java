package com.api.saojeong.domain;

import com.api.saojeong.Parking.enums.ParkingKind;
import com.api.saojeong.global.utill.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Auditable;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)

public class Parking extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parking_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "zipcode")
    private String zipcode;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "p_lat", nullable = false)
    private Double pLat;

    @Column(name = "p_lng", nullable = false)
    private Double pLng;

    @Enumerated(EnumType.STRING)
    @Column(name="kind")
    private ParkingKind kind;

    @Column(name="charge", nullable = false)
    private int charge;

    @Column(name = "image", nullable = false, length = 1000)
    private String image;

    @Column(name = "content")
    private String content;

    @Column(name="operate", nullable = false)
    private boolean operate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    @OneToMany(mappedBy = "parking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ParkingTime> parkingTimes = new ArrayList<>();

    @OneToMany(mappedBy = "parking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    // 연관관계 편의 메서드 (간단 버전)
    public void addParkingTime(ParkingTime parkingTime) {
        parkingTimes.add(parkingTime);
        parkingTime.setParking(this);
    }
}
