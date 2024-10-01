package com.squad.squad.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RatingScheduler {

    private final RatingService ratingService;

    public RatingScheduler(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @Scheduled(cron = "0 0 0 */3 * *") // 3 günde bir gece yarısı çalışacak
    public void clearRatingsEveryThreeDays() {
        ratingService.clearAllRatings();
    }

}
