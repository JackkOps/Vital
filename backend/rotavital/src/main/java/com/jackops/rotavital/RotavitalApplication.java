package com.jackops.rotavital;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RotavitalApplication {

	public static void main(String[] args) {
		SpringApplication.run(RotavitalApplication.class, args);
	}

}
