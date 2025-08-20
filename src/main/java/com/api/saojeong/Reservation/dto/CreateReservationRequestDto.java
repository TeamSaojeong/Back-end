package com.api.saojeong.Reservation.dto;

import lombok.Getter;
import org.springframework.web.bind.annotation.RequestParam;

@Getter
public class CreateReservationRequestDto {
    private int usingMinutes;

    private String placename;
    private String externalId;
    private String provider;
}
