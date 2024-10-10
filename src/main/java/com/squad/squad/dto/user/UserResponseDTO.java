package com.squad.squad.dto.user;

import com.squad.squad.dto.PlayerDTO;

import java.time.LocalDateTime;
import java.util.Date;

public class UserResponseDTO {
    private Integer id;
    private String username;
    private String role;
    private PlayerDTO player;
    private LocalDateTime createdAt;

    public UserResponseDTO() {
    }

    public UserResponseDTO(Integer id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public PlayerDTO getPlayer() {
        return player;
    }

    public void setPlayer(PlayerDTO player) {
        this.player = player;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}