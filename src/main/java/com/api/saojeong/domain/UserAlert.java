package com.api.saojeong.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_alerts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)         // 편하면 member_id 숫자 컬럼으로만 둬도 OK
    @JoinColumn(name = "member_id")
    private Member member;

    private String type;       // "SOONOUT"
    private Long soonoutId;
    private String title;
    private String body;
    private String deeplink;

    @Lob
    private String payloadJson;

    private OffsetDateTime createdAt;
    private OffsetDateTime consumedAt;
}