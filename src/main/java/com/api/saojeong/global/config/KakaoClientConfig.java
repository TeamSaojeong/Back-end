package com.api.saojeong.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class KakaoClientConfig {

    @Bean
    WebClient kakaoWebClient(@Value("${kakao.rest-api-key}") String apiKey) {

        return WebClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader("Authorization", "KakaoAK " + apiKey)
                .build();
    }
}