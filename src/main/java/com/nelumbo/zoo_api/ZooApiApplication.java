package com.nelumbo.zoo_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ZooApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZooApiApplication.class, args);
	}

}
