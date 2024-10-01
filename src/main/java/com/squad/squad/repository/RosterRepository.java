package com.squad.squad.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.squad.squad.entity.Roster;

public interface RosterRepository extends JpaRepository<Roster, Integer> {

    List<Roster> findRosterByGameId(Integer gameId);

    List<Roster> findRosterByGameIdAndTeamColor(Integer gameId, String team_color);

    void deleteByGameId(Integer game_id);
}
