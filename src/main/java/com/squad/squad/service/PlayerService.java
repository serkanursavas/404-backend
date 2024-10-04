package com.squad.squad.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.squad.squad.entity.Player;

@Service
public interface PlayerService {

    List<Player> getAllActivePlayers();

    Player getPlayerById(Integer id);

    Player updatePlayer(Player player);

    void deletePlayerById(Integer id);
}
