package com.api.saojeong.Parking.dto;

import com.api.saojeong.domain.ParkingTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CreateParkingResponseDto {
    //Long member_id;
    private Long parking_id;
    private String name;
    private List<ParkingTimeDto> availableTimes;
    private int charge;

}
