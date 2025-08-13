package com.api.saojeong.Parking.dto;

import lombok.Builder;

import java.util.List;
@Builder
public record GetDetailMemberParkingResponseDto(
        String parkingName,
        String address,
        String photo,
        String content,
        List<ParkingTimeDto> availableTimes,
        int charge

) {
}
