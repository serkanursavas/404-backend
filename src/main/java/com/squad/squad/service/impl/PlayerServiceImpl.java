package com.squad.squad.service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.squad.squad.entity.Player;
import com.squad.squad.exception.PlayerNotFoundException;
import com.squad.squad.repository.PlayerRepository;
import com.squad.squad.service.PlayerService;

import jakarta.transaction.Transactional;

@Service
public class PlayerServiceImpl implements PlayerService {
    private final PlayerRepository playerRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public List<Player> getAllActivePlayers() {
        return playerRepository.findByActive(true);
    }

    @Override
    public Player getPlayerById(Integer id) {
        return playerRepository.findByIdAndActive(id, true)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + id));
    }

    @Override
    @Transactional
    public Player createPlayer(Player player) {
        return playerRepository.save(player);
    }

    @Override
    @Transactional
    public Player updatePlayer(Player updatedPlayer) {
        Player existingPlayer = playerRepository.findById(updatedPlayer.getId())
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + updatedPlayer.getId()));

        BeanUtils.copyProperties(updatedPlayer, existingPlayer);
        existingPlayer.setActive(true);

        return playerRepository.save(existingPlayer);

    }

    @Override
    @Transactional
    public void deletePlayerById(Integer id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + id));

        playerRepository.delete(player);
    }

}
