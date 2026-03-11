package com.squad.squad.entity;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

@Audited
@Entity
@Table(name = "roster_persona")
public class RosterPersona extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "roster_id", nullable = false)
    private Roster roster;

    @ManyToOne
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Column(nullable = false)
    private Integer count = 0;

    public Roster getRoster() {
        return roster;
    }

    public void setRoster(Roster roster) {
        this.roster = roster;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
