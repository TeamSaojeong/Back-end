package com.api.saojeong.SoonOut.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateSoonOutRequestDto {
    private Long parkingId;
    private int soonMinute;
}
