package com.squad.squad.service;



import com.squad.squad.dto.AddPersonaRequestDTO;
import com.squad.squad.dto.PersonaCategoryChampionDTO;
import com.squad.squad.dto.PersonaLeaderboardEntryDTO;

import java.util.List;

public interface PersonaService {

    void savePersonas(Integer gameId, List<AddPersonaRequestDTO> personas);

    void recalculatePersonasForGame(Integer gameId);

    void resubmitPersonasForGame(Integer gameId, List<AddPersonaRequestDTO> personas);

    List<PersonaCategoryChampionDTO> getCategoryChampions();

    List<PersonaLeaderboardEntryDTO> getLeaderboardForPersona(Integer personaId);

}