package com.api.saojeong.Parking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
public class ParkingTimeDto {
        @NotBlank(message = "주차 가능 시작 시간을 입력해주세요.")
        @JsonFormat(pattern = "HH:mm")
        private LocalTime start;

        @NotBlank(message = "주차 가능 끝 시간을 입력해주세요.")
        @JsonFormat(pattern = "HH:mm")
        private LocalTime end;

}
