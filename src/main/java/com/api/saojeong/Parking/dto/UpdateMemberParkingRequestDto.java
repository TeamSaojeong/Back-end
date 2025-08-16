package com.api.saojeong.Parking.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.util.List;
@Getter
public class UpdateMemberParkingRequestDto {

    @Pattern(regexp = "^(?!\\s*$).+", message = "이름은 공백일 수 없습니다.")
    private String name;

    private String zipcode;

    @Pattern(regexp = "^(?!\\s*$).+", message = "주차 주소를 입력해주세요.")
    private String address;

    private String image;

    @Pattern(regexp = "^(?!\\s*$).+", message = "내용은 필수입니다.")
    private String content;

    @Size(min = 1, message = "주차 가능 시간을 1개 이상 입력해주세요.")
    private List<ParkingTimeDto> operateTimes;

    @Min(value = 0, message = "주차 요금은 0 이상이어야 합니다.")
    private Integer charge;

}
