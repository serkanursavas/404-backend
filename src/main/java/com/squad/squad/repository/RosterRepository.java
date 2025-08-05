package com.squad.squad.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.squad.squad.entity.Roster;

@Repository
public interface RosterRepository extends SecureJpaRepository<Roster, Integer> {

        @Query("SELECT r FROM Roster r WHERE r.game.id = :gameId AND r.groupId = :groupId")
        List<Roster> findByGameId(@Param("gameId") Integer gameId, @Param("groupId") Integer groupId);

        @Query("SELECT r FROM Roster r WHERE r.player.id = :playerId AND r.groupId = :groupId")
        List<Roster> findByPlayerId(@Param("playerId") Integer playerId, @Param("groupId") Integer groupId);

        // Eksik method'lar
        @Query("SELECT r FROM Roster r WHERE r.game.id = :gameId AND r.groupId = :groupId")
        List<Roster> findRosterByGameId(@Param("gameId") Integer gameId, @Param("groupId") Integer groupId);

        @Query("DELETE FROM Roster r WHERE r.game.id = :gameId AND r.groupId = :groupId")
        void deleteByGameId(@Param("gameId") Integer gameId, @Param("groupId") Integer groupId);

        @Query("SELECT r FROM Roster r WHERE r.game.id = :gameId AND r.teamColor = :teamColor AND r.groupId = :groupId")
        List<Roster> findRosterByGameIdAndTeamColor(@Param("gameId") Integer gameId,
                        @Param("teamColor") String teamColor,
                        @Param("groupId") Integer groupId);

        @Query("SELECT r FROM Roster r WHERE r.game.id = :gameId AND r.player.id = :playerId AND r.groupId = :groupId")
        List<Roster> findByGameIdAndPlayerId(@Param("gameId") Integer gameId, @Param("playerId") Integer playerId,
                        @Param("groupId") Integer groupId);

        @Query("SELECT r FROM Roster r WHERE r.game.id = :gameId AND r.groupId = :groupId")
        List<Roster> findAllByGameId(@Param("gameId") Integer gameId, @Param("groupId") Integer groupId);

        @Query("SELECT r FROM Roster r WHERE r.player.id = :playerId AND r.groupId = :groupId")
        List<Roster> findRosterByPlayerId(@Param("playerId") Integer playerId, @Param("groupId") Integer groupId);
}