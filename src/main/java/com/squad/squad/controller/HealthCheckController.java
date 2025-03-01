package com.squad.squad.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthCheckController {

    @GetMapping("/status")
    public String checkStatus() {
        return "🚀 Yeni kod deploy edildi! Timestamp: " + System.currentTimeMillis();
    }
}