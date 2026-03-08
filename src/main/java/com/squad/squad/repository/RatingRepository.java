package com.squad.squad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.squad.squad.entity.Rating;
import com.squad.squad.entity.Roster;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Integer> {

    List<Rating> findByRoster(Roster roster);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.roster.game.id = :game_id AND r.roster.teamColor = :team_color")
    Integer countByRosterGameIdAndTeamColor(@Param("game_id") Integer game_id, @Param("team_color") String team_color);

    @Query("SELECT AVG(r.rate) FROM Rating r WHERE r.roster = :roster")
    Double findAverageRatingByRoster(@Param("roster") Roster roster);

    boolean existsByPlayerIdAndRosterId(Integer playerId, Integer rosterId);

    boolean existsByPlayerId(Integer playerId);

    @Query("SELECT COUNT(r) > 0 FROM Rating r " +
           "WHERE r.player.id = :playerId " +
           "AND r.roster.game.isPlayed = true " +
           "AND r.roster.game.isVoted = false " +
           "AND r.roster.game.squad.id = :squadId")
    boolean existsByPlayerIdAndActiveGame(
        @Param("playerId") Integer playerId,
        @Param("squadId") Integer squadId);

    @Modifying
    @Query("DELETE FROM Rating r WHERE r.roster.game.squad.id = :squadId")
    void deleteAllBySquadId(@Param("squadId") Integer squadId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.roster.game.squad.id = :squadId")
    long countBySquadId(@Param("squadId") Integer squadId);
}
