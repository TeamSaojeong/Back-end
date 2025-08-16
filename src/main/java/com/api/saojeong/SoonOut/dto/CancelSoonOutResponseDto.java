package com.api.saojeong.SoonOut.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CancelSoonOutResponseDto {
    Long soonOutId;
    Boolean status;
}
