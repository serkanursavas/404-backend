package com.squad.squad.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetLink);
}
