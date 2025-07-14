package com.squad.squad.dto.group;

import java.time.LocalDateTime;

public class GroupRequestResponseDTO {
    private Integer id;
    private String groupName;
    private String groupDescription;
    private String status;
    private LocalDateTime requestedAt;
    private String requestedByUsername;
    private String intendedAdminUsername;

    // Constructors
    public GroupRequestResponseDTO() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getGroupDescription() { return groupDescription; }
    public void setGroupDescription(String groupDescription) { this.groupDescription = groupDescription; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public String getRequestedByUsername() { return requestedByUsername; }
    public void setRequestedByUsername(String requestedByUsername) { this.requestedByUsername = requestedByUsername; }

    public String getIntendedAdminUsername() { return intendedAdminUsername; }
    public void setIntendedAdminUsername(String intendedAdminUsername) { this.intendedAdminUsername = intendedAdminUsername; }
}