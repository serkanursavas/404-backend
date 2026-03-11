package com.squad.squad.entity;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

@Audited
@Entity
@Table(name = "persona")
public class Persona extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private String category;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
