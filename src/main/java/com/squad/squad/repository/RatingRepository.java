package com.squad.squad.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.squad.squad.entity.Rating;
import com.squad.squad.entity.Roster;

@Repository
public interface RatingRepository extends SecureJpaRepository<Rating, Integer> {

    @Query("SELECT r FROM Rating r WHERE r.player.id = :playerId")
    List<Rating> findByPlayerId(Integer playerId);

    @Query("SELECT r FROM Rating r WHERE r.roster.game.id = :gameId")
    List<Rating> findByGameId(Integer gameId);

    // Eksik method'lar
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Rating r WHERE r.player.id = :playerId AND r.roster.id = :rosterId")
    boolean existsByPlayerIdAndRosterId(@Param("playerId") Integer playerId, @Param("rosterId") Integer rosterId);

    @Query("SELECT AVG(r.rate) FROM Rating r WHERE r.roster = :roster")
    Double findAverageRatingByRoster(@Param("roster") Roster roster);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.roster.game.id = :gameId AND r.roster.teamColor = :teamColor")
    Integer countByRosterGameIdAndTeamColor(@Param("gameId") Integer gameId, @Param("teamColor") String teamColor);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Rating r WHERE r.player.id = :playerId AND r.groupId = :groupId")
    boolean existsByPlayerId(@Param("playerId") Integer playerId, @Param("groupId") Integer groupId);
}