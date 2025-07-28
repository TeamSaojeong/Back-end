package com.api.saojeong.global.config;

import java.util.Arrays;
import java.util.Collections;


import com.api.saojeong.Member.repository.MemberRepository;
import com.api.saojeong.Member.service.MemberQueryService;
import com.api.saojeong.global.handler.JwtLoginFailureHandler;
import com.api.saojeong.global.handler.JwtLoginSuccessHandler;
import com.api.saojeong.global.security.CustomUsernamePwdAuthenticationFilter;
import com.api.saojeong.global.security.JwtAuthenticationFilter;
import com.api.saojeong.global.security.JwtService;
import com.api.saojeong.global.security.LoginService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;


import lombok.RequiredArgsConstructor;

/**
 *  Spring Security config
 *  인증은 CustomJsonUsernamePasswordAuthenticationFilter에서 authenticate()로 인증된 사용자로 처리,
 *  JwtAuthenticationProcessingFilter는 AccessToken, RefreshToken 재발급
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final LoginService loginService;
    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;
    private final JwtLoginSuccessHandler jwtLoginSuccessHandler;
    private final JwtLoginFailureHandler jwtLoginFailureHandler;
    private final MemberQueryService memberQueryService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // csrf 보안 사용 X => Rest API
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS)) // Token 기반 인증 => session 사용 X
                .authorizeHttpRequests((requests) -> requests


                        .requestMatchers(HttpMethod.POST, "/api/login", "/api/signup").permitAll() // 허용된 주소
                        .requestMatchers("/error").permitAll()

                        .anyRequest().authenticated()
                )
                // CORS
                .cors(corsCustomizer -> corsCustomizer.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOriginPatterns(Collections.singletonList("*"));
                    config.setAllowedMethods(Collections.singletonList("*"));
                    config.setAllowCredentials(true);
                    config.setAllowedHeaders(Collections.singletonList("*"));
                    config.setExposedHeaders(Arrays.asList("Authorization", "Authorization-refresh"));
                    config.setMaxAge(3600L);
                    return config;
                }));
        http.addFilterAfter(customUsernamePwdAuthenticationFilter(), LogoutFilter.class);
        http.addFilterBefore(jwtAuthenticationProcessingFilter(), CustomUsernamePwdAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bcrypt encode
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(loginService);
        return new ProviderManager(provider);
    }

    @Bean
    public CustomUsernamePwdAuthenticationFilter customUsernamePwdAuthenticationFilter() {
        CustomUsernamePwdAuthenticationFilter customJsonUsernamePasswordLoginFilter
                = new CustomUsernamePwdAuthenticationFilter(objectMapper);
        customJsonUsernamePasswordLoginFilter.setAuthenticationManager(authenticationManager());
        customJsonUsernamePasswordLoginFilter.setAuthenticationSuccessHandler(jwtLoginSuccessHandler);
        customJsonUsernamePasswordLoginFilter.setAuthenticationFailureHandler(jwtLoginFailureHandler);
        return customJsonUsernamePasswordLoginFilter;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationProcessingFilter() {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService,
                memberRepository, memberQueryService);
        return jwtAuthenticationFilter;
    }

}