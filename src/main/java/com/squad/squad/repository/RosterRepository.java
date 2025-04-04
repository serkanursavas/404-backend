package com.squad.squad.repository;

import com.squad.squad.entity.Roster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RosterRepository extends JpaRepository<Roster, Integer> {

    List<Roster> findRosterByGameId(Integer gameId);

    List<Roster> findRosterByPlayerId(Integer player_id);

    List<Roster> findRosterByGameIdAndTeamColor(Integer gameId, String team_color);

    void deleteByGameId(Integer game_id);

    Roster findByGameIdAndPlayerId(Integer gameId, Integer playerId);

    List<Roster> findAllByGameId(Integer gameId);


}