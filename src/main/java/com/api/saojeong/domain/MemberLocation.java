package com.api.saojeong.domain;
import com.api.saojeong.global.utill.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "member_location",
        indexes = @Index(name = "idx_member_location_member", columnList = "member_id", unique = true))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MemberLocation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id", nullable = false, unique = true)
    private Member member;

    @Column(nullable = false) private Double lat;
    @Column(nullable = false) private Double lng;

    @Column(name="updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
