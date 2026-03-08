package com.squad.squad.dto.squad;

public class SquadSummaryDTO {
    private Integer id;
    private String name;
    private String role;
    private String adminName;
    private Integer memberCount;

    public SquadSummaryDTO() {}

    public SquadSummaryDTO(Integer id, String name, String role, String adminName, Integer memberCount) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.adminName = adminName;
        this.memberCount = memberCount;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }
}
