package com.api.saojeong.Member.dto;

import lombok.*;

@Data
public class SigninResponseDto {
    private String memberId;
    private String username;
    private String password;
    public SigninResponseDto( String memberId, String username, String password) {
        this.memberId = memberId;
        this.username = username;
        this.password = password;
    }
}
