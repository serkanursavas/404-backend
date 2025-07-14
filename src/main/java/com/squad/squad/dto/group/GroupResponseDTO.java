package com.squad.squad.dto.group;

import com.squad.squad.entity.Group;
import java.time.LocalDateTime;

public class GroupResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String createdByUsername;
    private String groupAdminUsername;

    // Constructors
    public GroupResponseDTO() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getCreatedByUsername() { return createdByUsername; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }

    public String getGroupAdminUsername() { return groupAdminUsername; }
    public void setGroupAdminUsername(String groupAdminUsername) { this.groupAdminUsername = groupAdminUsername; }
}