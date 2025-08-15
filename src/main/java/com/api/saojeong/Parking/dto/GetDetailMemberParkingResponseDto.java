package com.api.saojeong.Parking.dto;

import lombok.Builder;

import java.util.List;
@Builder
public record GetDetailMemberParkingResponseDto(
        String parkingName,
        String zipcode,
        String address,
        String image,
        String content,
        List<ParkingTimeDto> operateTimes,
        int charge

) {
}
