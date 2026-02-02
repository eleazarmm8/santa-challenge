package com.challenge.santa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SantaChallengeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SantaChallengeApplication.class, args);
	}

}
