package com.api.saojeong.Member.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
@Getter
@RequiredArgsConstructor
public enum Authority implements GrantedAuthority {
    ROLE_ADMIN(1L), ROLE_GUEST(2L), ROLE_USER(3L);

    private final Long id;


    @Override
    public String getAuthority() {
        return name();
    }
}