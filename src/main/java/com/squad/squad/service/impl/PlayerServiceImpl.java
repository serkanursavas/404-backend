package com.squad.squad.service.impl;

import java.util.List;

import com.squad.squad.dto.player.GetAllActivePlayersDTO;
import com.squad.squad.dto.player.GetAllPlayersDTO;
import com.squad.squad.dto.player.PlayerUpdateRequestDTO;
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
    public List<GetAllPlayersDTO> getAllPlayers() {
        return playerMapper.playersToGetAllPlayersDTOs(playerRepository.findAll());
    }

    @Override
    public PlayerDTO getPlayerById(Integer id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + id));

        return playerMapper.playerToPlayerDTO(player);
    }

    @Override
    public void updatePlayer(PlayerUpdateRequestDTO updatedPlayer) {
        Player existingPlayer = playerRepository.findById(updatedPlayer.getId())
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + updatedPlayer.getId()));

        BeanUtils.copyProperties(updatedPlayer, existingPlayer);

        playerRepository.save(existingPlayer);
    }

    @Override
    public void softDelete(PlayerDTO deletedPlayer) {
        Player existingPlayer = playerRepository.findById(deletedPlayer.getId())
                .orElseThrow(() -> new RuntimeException("Player not foundasasdas with id: " + deletedPlayer.getId()));

        existingPlayer.setActive(false);
        playerRepository.save(existingPlayer);
    }

    @Override
    public List<GetAllActivePlayersDTO> getAllActivePlayers() {
        return playerMapper.playersToGetAllActivePlayersDTOs(playerRepository.findByActive(true));
    }

    @Override
    public List<Player> findAllById(List<Integer> playerIds) {
        return playerRepository.findAllById(playerIds);
    }
}