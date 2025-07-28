package com.api.saojeong.global.handler;


import com.api.saojeong.global.security.JwtService;
import com.api.saojeong.global.utill.response.CustomApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * JWT 로그인 필터 Success Handler
 */
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    /**
     * 인증에 성공하면 Access Token과 Refresh Token을 생성한 후 반환
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        UserDetails principal = (UserDetails)authentication.getPrincipal();
        String loginId = principal.getUsername();
        log.info("Jwt Login Success :: Login ID = {}", loginId);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                CustomApiResponse.createSuccessWithoutData(HttpStatus.OK.value(), "로그인에 성공하였습니다.")));
        loginSuccess(response, loginId);
    }

    private void loginSuccess(HttpServletResponse response, String loginId) {
        String accessToken = jwtService.createAccessToken(loginId);
        String refreshToken = jwtService.createRefreshToken();
        response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
        response.addHeader(jwtService.getRefreshHeader(), "Bearer " + refreshToken);

        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        jwtService.updateRefreshToken(loginId, refreshToken);
    }
}