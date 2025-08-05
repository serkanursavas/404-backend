package com.squad.squad.mapper;

import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.dto.player.GetAllActivePlayersDTO;
import com.squad.squad.dto.player.GetAllPlayersDTO;
import com.squad.squad.dto.player.PlayerUpdateRequestDTO;
import com.squad.squad.entity.Player;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlayerMapper {

    public PlayerDTO playerToPlayerDTO(Player player) {
        if (player == null) {
            return null;
        }

        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setId(player.getId());
        playerDTO.setName(player.getName());
        playerDTO.setSurname(player.getSurname());
        playerDTO.setActive(player.isActive());
        playerDTO.setFoot(player.getFoot());
        playerDTO.setPhoto(player.getPhoto());
        playerDTO.setRating(player.getRating());
        playerDTO.setPosition(player.getPosition());

        return playerDTO;
    }

    public List<PlayerDTO> playersToPlayerDTOs(List<Player> players) {
        if (players == null) {
            return null;
        }

        return players.stream()
                .map(this::playerToPlayerDTO)
                .collect(Collectors.toList());
    }

    public PlayerUpdateRequestDTO playerToPlayerUpdateRequestDTO(Player player) {
        if (player == null) {
            return null;
        }

        PlayerUpdateRequestDTO playerUpdateRequestDTO = new PlayerUpdateRequestDTO();
        playerUpdateRequestDTO.setId(player.getId());
        playerUpdateRequestDTO.setName(player.getName());
        playerUpdateRequestDTO.setSurname(player.getSurname());
        playerUpdateRequestDTO.setFoot(player.getFoot());
        playerUpdateRequestDTO.setPhoto(player.getPhoto());
        playerUpdateRequestDTO.setPosition(player.getPosition());

        return playerUpdateRequestDTO;
    }

    public Player playerDTOToPlayer(PlayerDTO playerDTO) {
        if (playerDTO == null) {
            return null;
        }

        Player player = new Player();
        player.setId(playerDTO.getId());
        player.setName(playerDTO.getName());
        player.setSurname(playerDTO.getSurname());
        player.setActive(playerDTO.isActive());
        player.setFoot(playerDTO.getFoot());
        player.setPhoto(playerDTO.getPhoto());
        player.setRating(playerDTO.getRating());
        player.setPosition(playerDTO.getPosition());

        return player;
    }

    public List<GetAllPlayersDTO> playersToGetAllPlayersDTOs(List<Player> players) {
        if (players == null) {
            return null;
        }

        return players.stream()
                .map(this::playerToGetAllPlayersDTO)
                .collect(Collectors.toList());
    }

    private GetAllPlayersDTO playerToGetAllPlayersDTO(Player player) {
        if (player == null) {
            return null;
        }

        GetAllPlayersDTO getAllPlayersDTO = new GetAllPlayersDTO();
        getAllPlayersDTO.setId(player.getId());
        getAllPlayersDTO.setName(player.getName());
        getAllPlayersDTO.setSurname(player.getSurname());
        getAllPlayersDTO.setActive(player.isActive());

        return getAllPlayersDTO;
    }

    public GetAllActivePlayersDTO playerToGetAllActivePlayersDTO(Player player) {
        if (player == null) {
            return null;
        }

        GetAllActivePlayersDTO getAllActivePlayersDTO = new GetAllActivePlayersDTO();
        getAllActivePlayersDTO.setId(player.getId());
        getAllActivePlayersDTO.setPlayerName(player.getName());

        return getAllActivePlayersDTO;
    }

    public List<GetAllActivePlayersDTO> playersToGetAllActivePlayersDTOs(List<Player> players) {
        if (players == null) {
            return null;
        }

        return players.stream()
                .map(this::playerToGetAllActivePlayersDTO)
                .collect(Collectors.toList());
    }
}