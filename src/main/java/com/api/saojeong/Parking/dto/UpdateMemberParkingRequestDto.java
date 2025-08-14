package com.api.saojeong.Parking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;
@Getter
public class UpdateMemberParkingRequestDto {

    private String name;

    private String address;

    private String photo;

    private String content;

    private List<ParkingTimeDto> availableTimes;

    private Integer charge;
}
