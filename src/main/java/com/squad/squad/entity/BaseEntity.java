package com.squad.squad.entity;

import com.squad.squad.context.TenantContext;
import jakarta.persistence.*;

@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "group_id", nullable = false)
    private Integer groupId;

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    // Yeni entity oluşturulurken otomatik olarak mevcut tenant'ı set et
    @PrePersist
    public void prePersist() {
        if (this.groupId == null) {
            Integer currentTenantId = TenantContext.getTenantId();
            if (currentTenantId != null && currentTenantId > 0) {
                // Authenticated user varsa, onun tenant'ını kullan
                this.groupId = currentTenantId;
            } else {
                // Authentication yoksa (user registration gibi), pending group kullan
                this.groupId = 0; // Group 0 = Pending/Unassigned
            }
        }
    }

    // Update sırasında tenant değişikliğini engelle
    @PreUpdate
    public void preUpdate() {
        Integer currentTenantId = TenantContext.getTenantId();
        if (currentTenantId != null && !currentTenantId.equals(this.groupId)) {
            throw new IllegalStateException("Cannot modify entity belonging to different tenant. Entity tenant: " +
                    this.groupId + ", Current tenant: " + currentTenantId);
        }
    }
}