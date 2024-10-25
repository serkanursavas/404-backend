package com.squad.squad.service;

import java.util.List;

import com.squad.squad.dto.roster.RosterResponseDTO;
import com.squad.squad.entity.Roster;

public interface RosterService {

    Roster getRosterById(Integer id);

    List<RosterResponseDTO> findRosterByGameId(Integer gameId);

    void saveAllRosters(List<Roster> rosters);

    void updateAllRosters(List<Roster> rosters);

    void deleteRosterByGameId(Integer id);

    void updatePlayerGeneralRating(Integer gameId);

    List<Roster> findRosterByGameIdAndTeamColor(Integer gameId, String teamColor);

    List<Roster> findAllById(List<Integer> rosterIds);

    Roster getRosterByPlayerIdAndGameId(Integer gameId, Integer playerId);
}