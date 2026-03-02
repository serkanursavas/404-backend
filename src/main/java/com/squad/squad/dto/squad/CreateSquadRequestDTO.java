package com.squad.squad.dto.squad;

public class CreateSquadRequestDTO {
    private String squadName;
    private String playerName;
    private String playerSurname;
    private String playerPosition;
    private String playerFoot;

    public String getSquadName() {
        return squadName;
    }

    public void setSquadName(String squadName) {
        this.squadName = squadName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerSurname() {
        return playerSurname;
    }

    public void setPlayerSurname(String playerSurname) {
        this.playerSurname = playerSurname;
    }

    public String getPlayerPosition() {
        return playerPosition;
    }

    public void setPlayerPosition(String playerPosition) {
        this.playerPosition = playerPosition;
    }

    public String getPlayerFoot() {
        return playerFoot;
    }

    public void setPlayerFoot(String playerFoot) {
        this.playerFoot = playerFoot;
    }
}
