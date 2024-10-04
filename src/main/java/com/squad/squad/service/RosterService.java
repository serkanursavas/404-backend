package com.squad.squad.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.squad.squad.dto.RosterDTO;
import com.squad.squad.entity.Roster;

@Service
public interface RosterService {

    List<RosterDTO> getAllRosters();

    Roster getRosterById(Integer id);

    List<RosterDTO> findRosterByGameId(Integer gameId);

    Roster saveRoster(Roster roster);

    Roster updateRoster(RosterDTO updaaRoster);

    void updateRosters(List<Roster> rosters);

    void updateRatingsForGame(Integer gameId, String teamColor);

    void deleteRosterByGameId(Integer id);

    void updatePlayerGeneralRating(Integer gameId);

}
