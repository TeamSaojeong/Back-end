package com.api.saojeong.global.utill.Member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationMemberUtils {
    public String getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println((String) authentication.getPrincipal());
        return (String) authentication.getPrincipal();
    }
}