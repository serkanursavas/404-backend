package com.squad.squad.service;

import java.util.List;

import com.squad.squad.entity.Player;

public interface PlayerService {

    List<Player> getAllActivePlayers();

    Player getPlayerById(Integer id);

    Player createPlayer(Player player);

    Player updatePlayer(Player player);

    void deletePlayerById(Integer id);
}
