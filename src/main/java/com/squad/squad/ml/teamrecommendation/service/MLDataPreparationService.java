package com.squad.squad.ml.teamrecommendation.service;

import com.squad.squad.entity.Player;
import com.squad.squad.ml.teamrecommendation.dto.MLPlayerDTO;
import com.squad.squad.repository.GoalRepository;
import com.squad.squad.repository.PlayerPersonaRepository;
import com.squad.squad.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MLDataPreparationService {

    private final PlayerRepository playerRepository;
    private final GoalRepository goalRepository;
    private final PlayerPersonaRepository personaRepository;

    public MLDataPreparationService(PlayerRepository playerRepository,
                                    GoalRepository goalRepository,
                                    PlayerPersonaRepository personaRepository) {
        this.playerRepository    = playerRepository;
        this.goalRepository      = goalRepository;
        this.personaRepository   = personaRepository;
    }

    /**
     * Verilen playerId listesi için MLPlayerDTO listesi oluşturur.
     */
    public List<MLPlayerDTO> buildMlDtos(List<Integer> playerIds) {
        List<Player> players = playerRepository.findByIdIn(playerIds);

        return players.stream().map(p -> {
            // 1️⃣ goalsTotal
            long goalsTotal = goalRepository.countByPlayerId(p.getId());

            // 2️⃣ personas (en çok kullanılan 3 tanesi)
            List<String> personas = personaRepository
                    .getPersonas(p.getId())
                    .stream()
                    .map(pp -> pp.getPersona().getName())
                    .collect(Collectors.toList());

            // 3️⃣ recentForm (son 5 maçın ortalaması)
            List<Double> lastRatings = playerRepository.getLast5MatchRatingByPlayerId(p.getId());
            double recentForm = lastRatings.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(p.getRating());

            // 4️⃣ tam isim (opsiyonel)
            String fullName = p.getName() +
                    (p.getSurname() != null && !p.getSurname().isBlank()
                            ? " " + p.getSurname()
                            : "");

            // 5️⃣ DTO yarat
            return new MLPlayerDTO(
                    p.getId().longValue(),
                    fullName,
                    p.getRating(),
                    p.getPosition(),
                    (int) goalsTotal,
                    personas,
                    recentForm
            );
        }).collect(Collectors.toList());
    }
}