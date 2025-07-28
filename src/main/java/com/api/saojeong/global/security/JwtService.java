package com.api.saojeong.global.security;


import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;


import com.api.saojeong.Member.repository.MemberRepository;
import com.api.saojeong.global.utill.response.CustomApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;




import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Getter
@Slf4j
/**
 * JWT 관련 서비스
 */
public class JwtService {
    @Value("${spring.security.jwt.secretKey}")
    private String secretKey;
    @Value("${spring.security.jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${spring.security.jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    @Value("${spring.security.jwt.access.header}")
    private String accessHeader;

    @Value("${spring.security.jwt.refresh.header}")
    private String refreshHeader;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String LOGINID_CLAIM = "loginId";
    private static final String BEARER = "Bearer ";

    private final MemberRepository memberRepository;

    /**
     * AccessToken 생성
     */
    public String createAccessToken(String loginId) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(ACCESS_TOKEN_SUBJECT)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + accessTokenExpirationPeriod))
                .claim(LOGINID_CLAIM, loginId)
                .signWith(key).compact();
    }

    /**
     * RefreshToken 생성
     */
    public String createRefreshToken() {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(REFRESH_TOKEN_SUBJECT)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + refreshTokenExpirationPeriod))
                .signWith(key).compact();
    }

    /**
     * AccessToken 헤더에 실어서 보내기
     */

    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        response.setHeader(accessHeader, accessToken);
        log.info("재발급된 Access Token : {}", accessToken);
    }

    /**
     * AccessToken + RefreshToken 헤더에 실어서 보내기
     */
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);
        log.info("Access Token, Refresh Token 헤더 설정 완료");
    }

    /**
     * Token 내용 추출
     */
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    /**
     * 헤더에서 AccessToken 추출
     */
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    /**
     * AccessToken에서 LoginId 추출
     */
    public Optional<String> extractLoginId(String accessToken) {

        SecretKey key = Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();
        System.out.println(Optional.ofNullable(String.valueOf(claims.get("loginId"))));
        return Optional.ofNullable(String.valueOf(claims.get("loginId")));
    }

    /**
     * AccessToken 헤더 설정
     */
    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, accessToken);
    }

    /**
     * RefreshToken 헤더 설정
     */
    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader, refreshToken);
    }

    /**
     * RefreshToken DB 저장(업데이트)
     */
    public void updateRefreshToken(String memberId, String refreshToken) {
        memberRepository.findByMemberId(memberId)
                .ifPresentOrElse(
                        member -> member.changeRefreshToken(refreshToken),
                        () -> new UsernameNotFoundException("일치하는 회원이 없습니다.")
                );
    }

    /**
     * 유효한 서명의 토큰인지 검증
     */
    public boolean isTokenValid(String token) {
        SecretKey key = Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8));
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("유효하지 않은 토큰입니다. {}", e.getMessage());
            CustomApiResponse<?> failWithout = CustomApiResponse.createFailWithout(401, "유효하지 않은 토큰입니다.");
            return ResponseEntity.status(401).body(failWithout).hasBody();
        }
    }

}