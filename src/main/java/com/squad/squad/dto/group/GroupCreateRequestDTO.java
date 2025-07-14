package com.squad.squad.dto.group;

public class GroupCreateRequestDTO {
    private String groupName;
    private String groupDescription;
    private Integer intendedAdminUserId; // Grup admin olacak kullanıcının ID'si

    // Constructors
    public GroupCreateRequestDTO() {}

    public GroupCreateRequestDTO(String groupName, String groupDescription, Integer intendedAdminUserId) {
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.intendedAdminUserId = intendedAdminUserId;
    }

    // Getters and Setters
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getGroupDescription() { return groupDescription; }
    public void setGroupDescription(String groupDescription) { this.groupDescription = groupDescription; }

    public Integer getIntendedAdminUserId() { return intendedAdminUserId; }
    public void setIntendedAdminUserId(Integer intendedAdminUserId) { this.intendedAdminUserId = intendedAdminUserId; }
}