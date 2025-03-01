package com.squad.squad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class SquadApplication {

    private static final Logger logger = LoggerFactory.getLogger(SquadApplication.class);


    public static void main(String[] args) {

        SpringApplication.run(SquadApplication.class, args);
        logger.info("🚀 Yeni deploy başarılı! - " + System.currentTimeMillis());
        System.out.println("🚀 Yeni deploy başarılı! sout - " + System.currentTimeMillis());
        System.out.println("🚀 Yeni deploy başarılı! sout22 - " + System.currentTimeMillis());

    }
}