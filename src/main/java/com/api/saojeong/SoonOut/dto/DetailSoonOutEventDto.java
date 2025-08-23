package com.api.saojeong.SoonOut.dto;

import com.api.saojeong.alert.Enum.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
@Builder
public class DetailSoonOutEventDto {
    private Long soonOutId;
    private NotificationType notificationType;
    private int minutes;
    private String parkingName;

    //개인
    private Long parkingId;

    //공영, 민영
    private String provider;
    private String externalId;

    private LocalDateTime createdAt;
}
