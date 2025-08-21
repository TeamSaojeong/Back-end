package com.api.saojeong.global.security;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import com.api.saojeong.Member.enums.Authority;
import com.api.saojeong.Member.repository.MemberRepository;
import com.api.saojeong.Member.service.MemberQueryService;
import com.api.saojeong.domain.Member;

import com.api.saojeong.global.handler.RestAuthenticationEntryPoint;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;



import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
/**
 * JWT Authentication 필터
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final MemberQueryService memberQueryService;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    /**
     * 로그인 요청 시 JWT 검증 X
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getServletPath().equals(CustomUsernamePwdAuthenticationFilter.DEFAULT_LOGIN_REQUEST_URL) ||
                request.getServletPath().equals(CustomUsernamePwdAuthenticationFilter.DEFAULT_SIGNUP_REQUEST_URL);
    }

    /**
     * 	JWT 검증 후
     * 	요청에 Refresh Token 존재 -> Refresh Token 검증 후 Access Token, Refresh Token 생성
     * 	요청에 Refresh Token 존재 X -> Access Token 검증
     */
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//        String refreshToken = jwtService.extractRefreshToken(request)
//                .filter(jwtService::isTokenValid)
//                .orElse(null);
//
//        if (refreshToken != null) {
//            checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
//            return;
//        }
//
//        if (refreshToken == null) {
//            checkAccessTokenAndAuthentication(request, response, filterChain);
//        }
//    }
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        try {
            // 1) Refresh Token
            var refreshOpt = jwtService.extractRefreshToken(req);
            if (refreshOpt.isPresent()) {
                String refresh = refreshOpt.get();
                if (jwtService.isTokenValid(refresh)) {
                    checkRefreshTokenAndReIssueAccessToken(res, refresh);
                    return; // 재발급 후 종료
                } else {
                    // ★ 여기서 예외 던지지 말고 EntryPoint 호출
                    authenticationEntryPoint.commence(
                            req, res, new InsufficientAuthenticationException("Invalid refresh token"));
                    return;
                }
            }

            // 2) Access Token
            var accessOpt = jwtService.extractAccessToken(req);
            if (accessOpt.isPresent()) {
                String access = accessOpt.get();
                if (jwtService.isTokenValid(access)) {
                    jwtService.extractLoginId(access)
                            .flatMap(memberQueryService::getMemberWithAuthorities)
                            .ifPresent(this::saveAuthentication);
                    chain.doFilter(req, res);
                    return;
                } else {
                    authenticationEntryPoint.commence(
                            req, res, new InsufficientAuthenticationException("Invalid access token"));
                    return;
                }
            }

            // 3) 토큰 없음 → 퍼밋 경로면 통과, 보호 경로면 뒤에서 401 나가게(여기서는 일단 통과)
            chain.doFilter(req, res);

        } catch (Exception e) {
            // 혹시 모를 예외도 401로 표준화
            authenticationEntryPoint.commence(
                    req, res, new InsufficientAuthenticationException("Authentication failed", e));
        }
    }
    /**
     * Refresh Token이 유효한 지 검증 후 Access Token 재발급
     */
    public void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) {
        memberRepository.findByRefreshToken(refreshToken)
                .ifPresent(member -> {
                    String reIssuedRefreshToken = reIssueRefreshToken(member);
                    // AccessToken, RefreshToken response에 전달
                    jwtService.sendAccessAndRefreshToken(response, jwtService.createAccessToken(member.getMemberId()),
                            reIssuedRefreshToken);
                });
    }

    /**
     * Refresh Token 재발급
     */
    private String reIssueRefreshToken(Member member) {
        String reIssuedRefreshToken = jwtService.createRefreshToken();
        // 새로운 Refresh Token으로 업데이트
        member.changeRefreshToken(reIssuedRefreshToken);
        return reIssuedRefreshToken;
    }

    /**
     * Access Token 검증 후 인증
     */
    public void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                  FilterChain filterChain) throws ServletException, IOException {

        jwtService.extractAccessToken(request)
                .filter(jwtService::isTokenValid)
                .ifPresent(accessToken -> jwtService.extractLoginId(accessToken)
                        .ifPresent(loginId -> memberQueryService.getMemberWithAuthorities(loginId)
                                .ifPresent(this::saveAuthentication)));

        filterChain.doFilter(request, response);
    }

    /**
     * 검증된 토큰이면 인증
     */
    public void saveAuthentication(Member myMember) {
        String password = myMember.getPassword();
        if (password == null) { // 소셜 로그인 유저의 비밀번호 임의로 설정 하여 소셜 로그인 유저도 인증 되도록 설정
            password = UUID.randomUUID().toString();
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(Authority.ROLE_USER.toString()));

        UserDetails userDetailsUser = org.springframework.security.core.userdetails.User.builder()
                .username(myMember.getMemberId())
                .password(password)
                .authorities(authorities)
                .build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetailsUser, null,
                        authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println(SecurityContextHolder.getContext().getAuthentication());
    }
}