package com.api.saojeong.Parking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;
@Getter
public class UpdateMemberParkingRequestDto {

    private String name;

    private String zipcode;

    private String address;

    private String image;

    private String content;

    private List<ParkingTimeDto> operateTimes;

    private Integer charge;
}
