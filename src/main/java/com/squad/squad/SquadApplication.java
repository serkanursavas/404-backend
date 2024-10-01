package com.squad.squad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SquadApplication {

	public static void main(String[] args) {
		SpringApplication.run(SquadApplication.class, args);
	}

}
