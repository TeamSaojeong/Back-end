package com.api.saojeong.global.handler;


import com.api.saojeong.global.utill.response.CustomApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.core.AuthenticationException;

import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * JWT 로그인 필터 Failure Handler
 */
public class JwtLoginFailureHandler implements AuthenticationFailureHandler {

    // jackson으로 바디 변환
    private final ObjectMapper objectMapper;
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws
            IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(400);
        response.getWriter().write(objectMapper.writeValueAsString(
                CustomApiResponse.createFailWithout(HttpStatus.BAD_REQUEST.value(),"로그인에 실패했습니다.")));
        log.info("Jwt Login fail :: error = {}", exception.getMessage());
    }
}