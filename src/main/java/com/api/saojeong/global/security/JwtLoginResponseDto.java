package com.api.saojeong.global.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JwtLoginResponseDto {
    private String userName;
    private boolean validToken;
}
