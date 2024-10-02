package com.squad.squad.service;

import com.squad.squad.entity.Player;

import java.util.List;

public interface PlayerService {
    // tum oyunculari cagirma
    List<Player> getAllActivePlayers();

    // idye gore bir oyuncu bulma
    Player getPlayerById(Integer id);

    // update player
    Player updatePlayer(Player updatedPlayer);

    void deletePlayerById(Integer id);
}
