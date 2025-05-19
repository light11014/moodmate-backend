package com.moodmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MoodmateApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoodmateApplication.class, args);
	}

}
