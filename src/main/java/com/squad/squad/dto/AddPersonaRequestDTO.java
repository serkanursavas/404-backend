package com.squad.squad.dto;

import java.util.List;

public class AddPersonaRequestDTO {

    private Integer rosterId; // Oyuncunun Roster ID'si
    private List<Integer> personaIds; // Seçilen Persona ID'leri

    public Integer getRosterId() {
        return rosterId;
    }

    public void setRosterId(Integer rosterId) {
        this.rosterId = rosterId;
    }

    public List<Integer> getPersonaIds() {
        return personaIds;
    }

    public void setPersonaIds(List<Integer> personaIds) {
        this.personaIds = personaIds;
    }

}