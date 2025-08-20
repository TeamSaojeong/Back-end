package com.api.saojeong.domain;

import com.api.saojeong.alert.Enum.NotificationType;
import com.api.saojeong.global.utill.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "notification_event",
        uniqueConstraints = {
                // SoonOut 알림 중복 방지: (type, soonout_id, member_id) 유니크
                @UniqueConstraint(name = "uq_type_soonout_member",
                        columnNames = {"type", "soonout_id", "member_id"}),
                // 예약 알림 중복 방지: (type, reservation_id, member_id) 유니크
                @UniqueConstraint(name = "uq_type_reservation_member",
                        columnNames = {"type", "reservation_id", "member_id"})
        }
)
public class NotificationEvent extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 알림 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private NotificationType type;

    /** SoonOut 기반 알림이면 채움, 아니라면 null */
    @Column(name = "soonout_id")
    private Long soonOutId;

    /** 예약 기반 알림이면 채움, 아니라면 null */
    @Column(name = "reservation_id")
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "sent", nullable = false)
    private boolean sent;

    /** 선택: 디버깅/추적용으로 메세지 주제/해시 저장해도 좋음 */
    @Column(name = "meta", length = 255)
    private String meta;

}
