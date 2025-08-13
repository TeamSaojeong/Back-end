package com.api.saojeong.Parking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ParkingTimeDto {
        @NotBlank(message = "주차 가능 시작 시간을 입력해주세요.")
        @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "시작 시간 형식이 올바르지 않습니다. (HH:MM)")
        private String start;

        @NotBlank(message = "주차 가능 끝 시간을 입력해주세요.")
        @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "종료 시간 형식이 올바르지 않습니다. (HH:MM)")
        private String end;

}
