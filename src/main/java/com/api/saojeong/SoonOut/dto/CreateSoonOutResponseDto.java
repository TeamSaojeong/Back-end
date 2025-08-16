package com.api.saojeong.SoonOut.dto;

public record CreateSoonOutResponseDto(
        Long soonId,
        Long parkingId,
        Long reservationId,
        Double lat,
        Double lng,
        Boolean status
) {

}
