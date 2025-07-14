// 4. Membership Request DTOs
package com.squad.squad.dto.membership;

public class MembershipRequestDTO {
    private Integer groupId;

    // Constructors
    public MembershipRequestDTO() {}

    public MembershipRequestDTO(Integer groupId) {
        this.groupId = groupId;
    }

    // Getters and Setters
    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }
}