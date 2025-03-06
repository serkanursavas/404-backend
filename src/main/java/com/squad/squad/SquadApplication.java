package com.squad.squad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SquadApplication {



    public static void main(String[] args) {

        SpringApplication.run(SquadApplication.class, args);

    }
}