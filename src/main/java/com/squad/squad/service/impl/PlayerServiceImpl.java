package com.squad.squad.service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.entity.Player;
import com.squad.squad.exception.PlayerNotFoundException;
import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.repository.PlayerRepository;
import com.squad.squad.service.PlayerService;

import jakarta.transaction.Transactional;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper = PlayerMapper.INSTANCE;

    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public List<PlayerDTO> getAllActivePlayers() {
        return playerMapper.playersToPlayerDTOs(playerRepository.findByActive(true));
    }

    @Override
    public PlayerDTO getPlayerById(Integer id) {
        Player player = playerRepository.findByIdAndActive(id, true)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + id));

        return playerMapper.playerToPlayerDTO(player);
    }

    @Override
    @Transactional
    public Player createPlayer(Player player) {
        return playerRepository.save(player);
    }

    @Override
    @Transactional
    public PlayerDTO updatePlayer(PlayerDTO updatedPlayer) {
        Player existingPlayer = playerRepository.findById(updatedPlayer.getId())
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + updatedPlayer.getId()));

        BeanUtils.copyProperties(updatedPlayer, existingPlayer);
        existingPlayer.setActive(true);

        return playerMapper.playerToPlayerDTO(playerRepository.save(existingPlayer));

    }

    @Override
    @Transactional
    public void deletePlayerById(Integer id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + id));

        playerRepository.delete(player);
    }

}
