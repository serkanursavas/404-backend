package com.squad.squad.service.impl;

import java.util.List;

import com.squad.squad.service.PlayerService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.squad.squad.entity.Player;
import com.squad.squad.repository.PlayerRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    // tum oyunculari cagirma
    @Override
    public List<Player> getAllActivePlayers() {
        return playerRepository.findByActive(true);
    }

    // idye gore bir oyuncu bulma
    @Override
    public Player getPlayerById(Integer id) {
        // Oyuncu bulunmadığında özel bir hata fırlatılır
        return playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
    }

    // update player
    @Override
    public Player updatePlayer(Player updatedPlayer) {

        Player existingPlayer = playerRepository.findById(updatedPlayer.getId())
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + updatedPlayer.getId()));

        BeanUtils.copyProperties(updatedPlayer, existingPlayer);

        return playerRepository.save(existingPlayer);

    }

    @Override
    public void deletePlayerById(Integer id) {

        if (playerRepository.findById(id).isEmpty()) {
            throw new RuntimeException("the player not found: " + id);
        }

        playerRepository.deleteById(id);
    }
}
