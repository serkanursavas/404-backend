package com.squad.squad.entity;

import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MappedSuperclass
public abstract class BaseEntity {

    private static final Logger logger = LoggerFactory.getLogger(BaseEntity.class);

    @Column(name = "group_id", nullable = true)
    private Integer groupId;

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    // Yeni entity oluşturulurken otomatik olarak null group'ı set et
    @PrePersist
    public void prePersist() {
        if (this.groupId == null) {
            // Authentication yoksa (user registration gibi), null group kullan
            this.groupId = null; // Group null = Pending/Unassigned
            logger.debug("Set groupId to null (pending) for entity {}", this.getClass().getSimpleName());
        }
    }

    // Not: @PreUpdate kaldırıldı - SecureJpaRepository seviyesinde güvenlik
    // sağlanıyor
}