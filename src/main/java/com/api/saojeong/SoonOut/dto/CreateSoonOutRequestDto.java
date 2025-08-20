package com.api.saojeong.SoonOut.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateSoonOutRequestDto {

    @NotNull(message = "위도(lat)는 필수값입니다.")
    private double lat;                // 필수

    @NotNull(message = "경도(lng)는 필수값입니다.")
    private double lng;                // 필수

    @NotNull(message = "minute는 필수입니다.")
    @Min(value = 5, message = "minute은 5 또는 10이어야 합니다.")
    @Max(value = 10, message = "minute은 5 또는 10이어야 합니다.")
    private int minute;                // 필수 (5 or 10)

    // defaultValue = true
    private boolean status = true;

    private Long reservationId;
    private String placeName;
    private String address;

    // optional
    private Long parkingId;            // 내부
    private String provider;           // 외부
    private String externalId;         //민영, 공영


}
