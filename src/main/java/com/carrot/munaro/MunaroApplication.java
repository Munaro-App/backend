package com.carrot.munaro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MunaroApplication {

	public static void main(String[] args) {
		SpringApplication.run(MunaroApplication.class, args);
	}

}
