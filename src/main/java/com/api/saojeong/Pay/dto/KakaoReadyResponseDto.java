package com.api.saojeong.Pay.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoReadyResponseDto {
    private String tid;
    private String next_redirect_pc_url;
    private String next_redirect_mobile_url;
}
