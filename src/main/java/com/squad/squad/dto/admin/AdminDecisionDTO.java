package com.squad.squad.dto.admin;

public class AdminDecisionDTO {
    private String decision; // "APPROVE" or "REJECT"
    private String rejectionReason; // Sadece reject durumunda

    // Constructors
    public AdminDecisionDTO() {}

    public AdminDecisionDTO(String decision, String rejectionReason) {
        this.decision = decision;
        this.rejectionReason = rejectionReason;
    }

    // Getters and Setters
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}