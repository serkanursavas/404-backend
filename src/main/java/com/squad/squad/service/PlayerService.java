package com.squad.squad.service;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.squad.squad.entity.Player;
import com.squad.squad.exception.PlayerNotFoundException;
import com.squad.squad.repository.PlayerRepository;

import jakarta.transaction.Transactional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;

    }

    public List<Player> getAllActivePlayers() {
        return playerRepository.findByActive(true);
    }

    public Player getPlayerById(Integer id) {
        return playerRepository.findByIdAndActive(id, true)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + id));
    }

    @Transactional
    public Player updatePlayer(Player updatedPlayer) {
        Player existingPlayer = playerRepository.findById(updatedPlayer.getId())
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + updatedPlayer.getId()));

        BeanUtils.copyProperties(updatedPlayer, existingPlayer);
        existingPlayer.setActive(true);

        return playerRepository.save(existingPlayer);

    }

    @Transactional
    public void deletePlayerById(Integer id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + id));

        playerRepository.delete(player);
    }

}
