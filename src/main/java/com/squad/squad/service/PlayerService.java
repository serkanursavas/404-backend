package com.squad.squad.service;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.squad.squad.entity.Player;
import com.squad.squad.repository.PlayerRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    // tum oyunculari cagirma
    public List<Player> getAllActivePlayers() {
        return playerRepository.findByActive(true);
    }

    // idye gore bir oyuncu bulma
    public Player getPlayerById(Integer id) {
        // Oyuncu bulunmadığında özel bir hata fırlatılır
        return playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
    }

    // update player
    public Player updatePlayer(Player updatedPlayer) {

        Player existingPlayer = playerRepository.findById(updatedPlayer.getId())
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + updatedPlayer.getId()));

        BeanUtils.copyProperties(updatedPlayer, existingPlayer);

        return playerRepository.save(existingPlayer);

    }

    public void deletePlayerById(Integer id) {

        if (playerRepository.findById(id).isEmpty()) {
            throw new RuntimeException("the player not found: " + id);
        }

        playerRepository.deleteById(id);
    }
}
