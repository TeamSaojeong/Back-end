package com.api.saojeong.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class AiClientConfig {
    @Bean
    WebClient parkingClient() {
        // Python 서비스 주소 (Docker compose 사용시 서비스명:port)
        return WebClient.builder()
                //.baseUrl("http://localhost:8000") // or "http://python-parking:8000"
                .baseUrl("http://localhost:8000")
                .build();
    }
}