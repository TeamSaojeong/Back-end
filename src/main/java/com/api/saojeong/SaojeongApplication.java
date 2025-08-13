package com.api.saojeong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SaojeongApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaojeongApplication.class, args);
	}

}
