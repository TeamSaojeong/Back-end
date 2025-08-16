package com.api.saojeong.global.security;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
/**
 * JWT 로그인 POST 요청 왔을 때 인증 필터
 */
public class CustomUsernamePwdAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public static final String DEFAULT_LOGIN_REQUEST_URL = "/api/login";
    public static final String DEFAULT_SIGNUP_REQUEST_URL = "/api/signup";
    private static final String HTTP_METHOD = "POST";
    private static final String CONTENT_TYPE = "application/json";
    private static final String LOGIN_ID_KEY = "loginId";
    private static final String PASSWORD_KEY = "password";

    private static final AntPathRequestMatcher DEFAULT_LOGIN_PATH_REQUEST_MATCHER =
            new AntPathRequestMatcher(DEFAULT_LOGIN_REQUEST_URL, HTTP_METHOD);

    private static final AntPathRequestMatcher DEFAULT_SIGNUP_PATH_REQUEST_MATCHER =
            new AntPathRequestMatcher(DEFAULT_SIGNUP_REQUEST_URL, HTTP_METHOD);

    private final ObjectMapper objectMapper;

    public CustomUsernamePwdAuthenticationFilter(ObjectMapper objectMapper) {
        super(DEFAULT_LOGIN_PATH_REQUEST_MATCHER);// 기존 formlogin (/login) 형태를 변경

        this.objectMapper = objectMapper;
    }
    @Override
    public boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        // 로그인과 회원가입 요청은 모두 필터에서 제외
        if ( DEFAULT_SIGNUP_PATH_REQUEST_MATCHER.matches(request)) {
            return false;
        }
        return super.requiresAuthentication(request, response); // 기본 로그인 요청은 처리
    }


    /**
     * JWT 로컬 로그인 인증 과정
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
            AuthenticationException,
            IOException,
            ServletException {
        // 지원하는 ContentType이 아닌 경우
        if (request.getContentType() == null || !request.getContentType().equals(CONTENT_TYPE)) {
            throw new AuthenticationServiceException(
                    "Authentication Content-Type not supported: " + request.getContentType());
        }

        // request body에서 로그인 ID와 비밀번호 추출
        String messageBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);

        Map<String, String> usernamePasswordMap = objectMapper.readValue(messageBody, Map.class);
        String loginId = usernamePasswordMap.get(LOGIN_ID_KEY);
        String password = usernamePasswordMap.get(PASSWORD_KEY);

        // principal 과 credentials 전달
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(loginId, password);
        return this.getAuthenticationManager().authenticate(authRequest);
    }
}