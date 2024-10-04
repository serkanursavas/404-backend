package com.squad.squad.service;

import java.util.List;

import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.entity.Player;

public interface PlayerService {

    List<PlayerDTO> getAllActivePlayers();

    PlayerDTO getPlayerById(Integer id);

    Player createPlayer(Player player);

    PlayerDTO updatePlayer(PlayerDTO player);

    void deletePlayerById(Integer id);

    void softDelete(PlayerDTO deletedPlayer);
}
