package com.squad.squad.dto.user;

public class ForgotPasswordResultDTO {
    private String username;
    private String email;

    public ForgotPasswordResultDTO(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
