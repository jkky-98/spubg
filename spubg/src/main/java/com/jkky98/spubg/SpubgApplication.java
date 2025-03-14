package com.jkky98.spubg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpubgApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpubgApplication.class, args);
	}

}
