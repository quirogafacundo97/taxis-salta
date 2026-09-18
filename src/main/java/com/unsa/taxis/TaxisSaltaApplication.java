package com.unsa.taxis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TaxisSaltaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaxisSaltaApplication.class, args);
	}

}
