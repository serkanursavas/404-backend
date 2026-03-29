package com.squad.squad.service;



import com.squad.squad.dto.AddPersonaRequestDTO;

import java.util.List;

public interface PersonaService {

    void savePersonas(List<AddPersonaRequestDTO> ratings);

    void recalculatePersonasForGame(Integer gameId);

    void resubmitPersonasForGame(Integer gameId, List<AddPersonaRequestDTO> personas);

}