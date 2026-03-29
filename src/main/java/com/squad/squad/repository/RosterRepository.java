package com.squad.squad.repository;

import com.squad.squad.entity.Roster;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RosterRepository extends JpaRepository<Roster, Integer> {

    @Query("SELECT r FROM Roster r JOIN FETCH r.player WHERE r.game.id = :gameId")
    List<Roster> findRosterByGameId(@Param("gameId") Integer gameId);

    List<Roster> findRosterByPlayerId(Integer player_id);

    List<Roster> findRosterByGameIdAndTeamColor(Integer gameId, String team_color);

    void deleteByGameId(Integer game_id);

    Roster findByGameIdAndPlayerId(Integer gameId, Integer playerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Roster r WHERE r.game.id = :gameId AND r.player.id = :playerId")
    Roster findByGameIdAndPlayerIdWithLock(@Param("gameId") Integer gameId, @Param("playerId") Integer playerId);

    List<Roster> findAllByGameId(Integer gameId);

    @Query("SELECT r FROM Roster r WHERE r.player.id = :playerId AND r.game.squad.id = :squadId")
    List<Roster> findRosterByPlayerIdAndSquadId(
        @Param("playerId") Integer playerId,
        @Param("squadId") Integer squadId);

    long countByGameId(Integer gameId);

    long countByGameIdAndHasPersonaVoteTrue(Integer gameId);

}