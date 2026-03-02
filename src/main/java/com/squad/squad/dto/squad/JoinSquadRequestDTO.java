package com.squad.squad.dto.squad;

public class JoinSquadRequestDTO {
    private String inviteCode;
    private String playerName;
    private String playerSurname;
    private String playerPosition;
    private String playerFoot;

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
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
