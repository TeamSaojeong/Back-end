package com.api.saojeong.domain;

import com.api.saojeong.Report.enums.ReportType;
import com.api.saojeong.global.utill.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Report extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="report_id")
    private Long id;

    @Column(name="report_num")
    private String carNum;

    @Column(name="report_image")
    private String image;

    @Enumerated(value = EnumType.STRING)
    @Column(name="report_type", nullable = false)
    private ReportType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="parking_id")
    private Parking parking;

}
