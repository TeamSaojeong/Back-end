package com.api.saojeong.domain;

import com.api.saojeong.global.utill.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="notification_event",
        uniqueConstraints = @UniqueConstraint(name="uq_soonout_member", columnNames={"soonout_id", "member_id"}))
public class NotificationEvent extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="soonout_id", nullable = false)
    private Long soonOutId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id", nullable = false)
    private Member member;

    @Column(name="sent", nullable = false)
    private boolean sent;
}