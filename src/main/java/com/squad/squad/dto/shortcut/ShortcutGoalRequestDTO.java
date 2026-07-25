package com.squad.squad.dto.shortcut;

public class ShortcutGoalRequestDTO {

    private String player;

    public ShortcutGoalRequestDTO() {
    }

    public ShortcutGoalRequestDTO(String player) {
        this.player = player;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }
}
