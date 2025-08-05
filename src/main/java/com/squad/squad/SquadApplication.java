package com.squad.squad;

import java.time.ZoneId;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SquadApplication {

    public static void main(String[] args) {

        TimeZone defaultTimeZone = TimeZone.getDefault();
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Logger logger = LoggerFactory.getLogger(SquadApplication.class);
        logger.info("Default TimeZone: " + defaultTimeZone.getID());
        logger.info("Default ZoneId: " + defaultZoneId.getId());

        SpringApplication.run(SquadApplication.class, args);

    }
}