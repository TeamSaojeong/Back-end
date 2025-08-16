package com.api.saojeong.Reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public record CreateReservationResponseDto(
        Long reservationId,
        Long memberId,
        String parkName,
        @JsonFormat(pattern = "HH:mm")
        LocalTime start,
        @JsonFormat(pattern = "HH:mm")
        LocalTime end,
        int usingMinutes
) {
}
