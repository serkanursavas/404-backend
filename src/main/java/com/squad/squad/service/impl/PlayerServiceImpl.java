package com.squad.squad.service.impl;

import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.dto.PlayerPersonaDTO;
import com.squad.squad.dto.TopListsDTO;
import com.squad.squad.dto.player.GetAllActivePlayersDTO;
import com.squad.squad.dto.player.PlayerUpdateRequestDTO;
import com.squad.squad.entity.Player;
import com.squad.squad.exception.PlayerNotFoundException;
import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.repository.PersonaRepository;
import com.squad.squad.repository.PlayerPersonaRepository;
import com.squad.squad.repository.PlayerRepository;
import com.squad.squad.service.PlayerService;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerPersonaRepository playerPersonaRepository;
    private final PersonaRepository personaRepository;
    private final PlayerMapper playerMapper;

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository, PlayerPersonaRepository playerPersonaRepository, PersonaRepository personaRepository, PlayerMapper playerMapper) {
        this.playerRepository = playerRepository;
        this.playerPersonaRepository = playerPersonaRepository;
        this.personaRepository = personaRepository;
        this.playerMapper = playerMapper;
    }

    @Override
    public List<PlayerDTO> getAllPlayers() {

        List<Player> players = playerRepository.findAll(); // Lazy Load
        return players.stream()
                .map(player -> {
                    PlayerDTO playerDTO = playerMapper.playerToPlayerDTO(player);

                    // PlayerPersona verilerini manuel olarak DTO'ya ekle
                    List<PlayerPersonaDTO> personas = player.getPlayerPersonas().stream()
                            .sorted((p1, p2) -> Integer.compare(p2.getCount(), p1.getCount())) // count DESC sıralama
                            .limit(3) // İlk 3 sonucu al
                            .map(playerPersona -> {
                                PlayerPersonaDTO dto = new PlayerPersonaDTO();
                                dto.setPersonaId(playerPersona.getPersona().getId());
                                dto.setPersonaName(playerPersona.getPersona().getName());
                                dto.setPersonaDescription(playerPersona.getPersona().getDescription());
                                dto.setCount(playerPersona.getCount());
                                dto.setCategory(playerPersona.getPersona().getCategory());
                                return dto;
                            })
                            .toList();

                    playerDTO.setPersonas(personas);
                    return playerDTO;
                })
                .toList();
    }

    @Override
    public PlayerDTO getPlayerById(Integer id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + id));



        PlayerDTO playerDTO = playerMapper.playerToPlayerDTO(player);

        List<PlayerPersonaDTO> personas = player.getPlayerPersonas().stream()
                .sorted((p1, p2) -> Integer.compare(p2.getCount(), p1.getCount())) // count DESC sıralama
                .limit(3) // İlk 3 sonucu al
                .map(playerPersona -> {
                    PlayerPersonaDTO dto = new PlayerPersonaDTO();
                    dto.setPersonaId(playerPersona.getPersona().getId());
                    dto.setPersonaName(playerPersona.getPersona().getName());
                    dto.setPersonaDescription(playerPersona.getPersona().getDescription());
                    dto.setCount(playerPersona.getCount());
                    dto.setCategory(playerPersona.getPersona().getCategory());
                    return dto;
                })
                .toList();

        playerDTO.setPersonas(personas);


        return playerDTO;
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

    public List<TopListsDTO> getTopRatedPlayersWithoutRecentGames() {
        // 1. Top Rated oyuncuları al
        List<Object[]> topRatedPlayers = playerRepository.findTopRatedPlayers();
        List<TopListsDTO> topRatedList = topRatedPlayers.stream()
                .map(record -> new TopListsDTO(
                        (Integer) record[0],    // playerId
                        (String) record[1],     // name
                        (String) record[2],     // surname
                        (Double) record[3]      // rating
                ))
                .toList();

        // 2. Son 2 maçtaki oyuncuları al
        List<Integer> recentGamePlayerIds = playerRepository.findPlayersInRecentGames();

        // 3. Filtreleme
        return topRatedList.stream()
                .filter(player -> recentGamePlayerIds.contains(player.getPlayerId()))
                .limit(5)  // En iyi 5 oyuncuyu al
                .collect(Collectors.toList());
    }

    @Cacheable(value = "playerCache")
    @Transactional
    public Map<Integer, PlayerDTO> findPlayersByIds(List<Integer> playerIds) {
        // Player bilgilerini repository üzerinden topluca çekelim
        List<Player> players = playerRepository.findByIdIn(playerIds);

        // Player nesnelerini Map yapısına dönüştürüp döndür
        return players.stream()
                .collect(Collectors.toMap(Player::getId, player -> {
                    PlayerDTO playerDTO = new PlayerDTO();
                    playerDTO.setId(player.getId());
                    playerDTO.setName(player.getName());
                    playerDTO.setSurname(player.getSurname());
                    // Diğer gerekli alanları burada set edebilirsiniz
                    return playerDTO;
                }));
    }
}