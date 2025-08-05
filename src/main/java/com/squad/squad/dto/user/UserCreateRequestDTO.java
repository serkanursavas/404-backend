package com.squad.squad.dto.user;

import com.squad.squad.dto.PlayerCreateDTO;

public class UserCreateRequestDTO {
    private String username;
    private String password;
    private String passwordAgain;
    private PlayerCreateDTO playerCreateDTO;

    // Group creation fields
    private String groupName;
    private String groupDescription;
    private Boolean createGroup;

    public UserCreateRequestDTO() {
    }

    public UserCreateRequestDTO(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordAgain() {
        return passwordAgain;
    }

    public void setPasswordAgain(String passwordAgain) {
        this.passwordAgain = passwordAgain;
    }

    public PlayerCreateDTO getPlayerCreateDTO() {
        return playerCreateDTO;
    }

    public void setPlayerCreateDTO(PlayerCreateDTO playerCreateDTO) {
        this.playerCreateDTO = playerCreateDTO;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public Boolean getCreateGroup() {
        return createGroup;
    }

    public void setCreateGroup(Boolean createGroup) {
        this.createGroup = createGroup;
    }
}