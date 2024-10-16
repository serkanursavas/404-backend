package com.squad.squad.service;

import java.util.List;

import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.dto.player.GetAllActivePlayersDTO;
import com.squad.squad.dto.player.GetAllPlayersDTO;
import com.squad.squad.dto.player.PlayerUpdateRequestDTO;
import com.squad.squad.entity.Player;

public interface PlayerService {

    List<GetAllPlayersDTO> getAllPlayers();

    PlayerDTO getPlayerById(Integer id);

    void updatePlayer(PlayerUpdateRequestDTO player);

    void softDelete(PlayerDTO deletedPlayer);

    List<GetAllActivePlayersDTO> getAllActivePlayers();

    List<Player> findAllById(List<Integer> playerIds);
}