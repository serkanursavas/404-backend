package com.squad.squad.repository;

import com.squad.squad.entity.PlayerPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerPersonaRepository extends JpaRepository<PlayerPersona, Integer> {
    Optional<PlayerPersona> findByPlayerIdAndPersonaId(Integer playerId, Integer personaId);

    Optional<PlayerPersona> findByPlayerIdAndPersonaIdAndSquadId(Integer playerId, Integer personaId, Integer squadId);

    @Query(value = "SELECT pp.*\n" +
            "FROM player_persona pp\n" +
            "JOIN persona per ON pp.persona_id = per.id\n" +
            "WHERE pp.player_id = :playerId\n" +
            "ORDER BY pp.count DESC\n" +
            "LIMIT 3\n", nativeQuery = true)
    List<PlayerPersona> getPersonas(Integer playerId);

    // Her kategoride en çok o kategori rozetini toplayan oyuncu (MVP/special kategorisi hariç, misafir oyuncular hariç).
    @Query(value = """
    WITH category_totals AS (
        SELECT pp.player_id AS playerId, per.category AS category, SUM(pp.count) AS total
        FROM player_persona pp
        JOIN persona per ON per.id = pp.persona_id
        JOIN player pl ON pl.id = pp.player_id
        WHERE pp.squad_id = :squadId AND per.category <> 'special' AND pl.is_guest = false
        GROUP BY pp.player_id, per.category
        HAVING SUM(pp.count) > 0
    ),
    ranked AS (
        SELECT *, ROW_NUMBER() OVER (PARTITION BY category ORDER BY total DESC, playerId) AS rn
        FROM category_totals
    )
    SELECT r.category AS category,
           pl.id AS playerId,
           pl.name AS name,
           pl.surname AS surname,
           pl.position AS position,
           r.total AS total
    FROM ranked r
    JOIN player pl ON pl.id = r.playerId
    WHERE r.rn = 1
    ORDER BY r.category
    """, nativeQuery = true)
    List<Object[]> findCategoryChampions(@Param("squadId") Integer squadId);

    // Belirli bir persona'yı en çok kazanan oyuncular (misafir oyuncular hariç).
    @Query(value = """
    SELECT pl.id AS playerId, pl.name AS name, pl.surname AS surname, pl.position AS position, pp.count AS count
    FROM player_persona pp
    JOIN player pl ON pl.id = pp.player_id
    WHERE pp.persona_id = :personaId AND pp.squad_id = :squadId AND pp.count > 0 AND pl.is_guest = false
    ORDER BY pp.count DESC, pl.name
    LIMIT 10
    """, nativeQuery = true)
    List<Object[]> findLeaderboardForPersona(@Param("personaId") Integer personaId, @Param("squadId") Integer squadId);
}
