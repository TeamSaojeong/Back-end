package com.api.saojeong.Parking.dto;

public record GetMemberParkingResponseDto(
        Long parkingId,
        String parkingName,
        Boolean operate

){
}
