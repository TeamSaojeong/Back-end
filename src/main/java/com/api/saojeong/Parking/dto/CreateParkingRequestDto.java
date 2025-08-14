package com.api.saojeong.Parking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateParkingRequestDto {
    @NotBlank(message = "주차 장소 이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "주차 주소를 입력해주세요.")
    private String address;

    //@NotBlank(message = "주차 공간 사진을 추가해주세요.")
    //private String photo;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @NotEmpty(message = "주차 가능 시간을 1개 이상 입력해주세요.")
    private List<ParkingTimeDto> operateTimes;

    @Min(value = 0, message = "주차 요금은 0 이상이어야 합니다.")
    private int charge;

}
