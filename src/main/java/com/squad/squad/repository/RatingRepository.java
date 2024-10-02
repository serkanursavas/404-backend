package com.squad.squad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.squad.squad.entity.Rating;
import com.squad.squad.entity.Roster;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Integer> {

    List<Rating> findByRoster(Roster roster);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.roster.game.id = :game_id AND r.roster.teamColor = :team_color")
    Integer countByRosterGameIdAndTeamColor(@Param("game_id") Integer game_id, @Param("team_color") String team_color);

    Double findAverageRatingByRoster(Roster roster);
}
