package com.api.saojeong.Reservation.dto;

public record CheckOutReservationResponseDto(
        Long memberId,
        Boolean reservationStatus,
        Boolean soonOutStatus
) {
}
