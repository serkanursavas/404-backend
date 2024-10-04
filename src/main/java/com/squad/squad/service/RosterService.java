package com.squad.squad.service;

import java.util.List;

import com.squad.squad.dto.RosterDTO;
import com.squad.squad.entity.Roster;

public interface RosterService {

    List<RosterDTO> getAllRosters();

    Roster getRosterById(Integer id);

    List<RosterDTO> findRosterByGameId(Integer gameId);

    Roster saveRoster(Roster roster);

    void saveAllRosters(List<Roster> rosters);

    Roster updateRoster(RosterDTO updaaRoster);

    void updateAllRosters(List<RosterDTO> rosters);

    void updateRatingsForGame(Integer gameId, String teamColor);

    void deleteRosterByGameId(Integer id);

    void updatePlayerGeneralRating(Integer gameId);

}
