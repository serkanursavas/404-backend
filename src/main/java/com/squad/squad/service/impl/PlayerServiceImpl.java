package com.squad.squad.service.impl;

import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.dto.PlayerPersonaDTO;
import com.squad.squad.dto.TopListProjection;
import com.squad.squad.dto.TopListsDTO;
import com.squad.squad.dto.player.GetAllActivePlayersDTO;
import com.squad.squad.dto.player.PlayerUpdateRequestDTO;
import com.squad.squad.entity.Player;
import com.squad.squad.exception.PlayerNotFoundException;
import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.repository.PersonaRepository;
import com.squad.squad.repository.PlayerPersonaRepository;
import com.squad.squad.repository.PlayerRepository;
import com.squad.squad.service.BaseSquadService;
import com.squad.squad.service.PlayerService;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlayerServiceImpl extends BaseSquadService implements PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerPersonaRepository playerPersonaRepository;
    private final PersonaRepository personaRepository;
    private final PlayerMapper playerMapper;

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository, PlayerPersonaRepository playerPersonaRepository,
                             PersonaRepository personaRepository, PlayerMapper playerMapper) {
        this.playerRepository = playerRepository;
        this.playerPersonaRepository = playerPersonaRepository;
        this.personaRepository = personaRepository;
        this.playerMapper = playerMapper;
    }

    @Override
    public List<PlayerDTO> getAllPlayers() {
        Integer squadId = getSquadId();
        List<Player> players = playerRepository.findBySquadId(squadId);
        Collator trCollator = Collator.getInstance(new Locale("tr", "TR"));

        players.sort(Comparator
                .comparing(Player::getName, trCollator)
                .thenComparing(Player::getSurname, trCollator));
        return players.stream()
                .map(player -> {
                    PlayerDTO playerDTO = playerMapper.playerToPlayerDTO(player);

                    List<PlayerPersonaDTO> personas = player.getPlayerPersonas().stream()
                            .sorted((p1, p2) -> Integer.compare(p2.getCount(), p1.getCount()))
                            .limit(3)
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

                    List<Double> last5GameRating = playerRepository.getLast5MatchRatingByPlayerId(player.getId(), squadId);
                    playerDTO.setLast5GameRating(last5GameRating);

                    return playerDTO;
                })
                .toList();
    }

    @Override
    public PlayerDTO getPlayerById(Integer id) {
        Player player = playerRepository.findByIdAndSquadId(id, getSquadId())
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + id));

        PlayerDTO playerDTO = playerMapper.playerToPlayerDTO(player);

        List<PlayerPersonaDTO> personas = player.getPlayerPersonas().stream()
                .sorted((p1, p2) -> Integer.compare(p2.getCount(), p1.getCount()))
                .limit(3)
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

        Integer squadId = getSquadId();
        if (squadId != null) {
            List<Double> last5GameRating = playerRepository.getLast5MatchRatingByPlayerId(player.getId(), squadId);
            playerDTO.setLast5GameRating(last5GameRating);
        }

        return playerDTO;
    }

    @Override
    public void updatePlayer(PlayerUpdateRequestDTO updatedPlayer) {
        Player existingPlayer = playerRepository.findByIdAndSquadId(updatedPlayer.getId(), getSquadId())
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + updatedPlayer.getId()));

        BeanUtils.copyProperties(updatedPlayer, existingPlayer);
        playerRepository.save(existingPlayer);
    }

    @Override
    public void softDelete(PlayerDTO deletedPlayer) {
        Player existingPlayer = playerRepository.findById(deletedPlayer.getId())
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + deletedPlayer.getId()));

        existingPlayer.setActive(false);
        playerRepository.save(existingPlayer);
    }

    @Override
    public List<GetAllActivePlayersDTO> getAllActivePlayers() {
        Integer squadId = getSquadId();
        return playerMapper.playersToGetAllActivePlayersDTOs(playerRepository.findByActiveAndSquadId(true, squadId));
    }

    @Override
    public List<Player> findAllById(List<Integer> playerIds) {
        return playerRepository.findAllById(playerIds);
    }

    @Override
    public List<Player> findAllByIdsInCurrentSquad(List<Integer> ids) {
        return playerRepository.findByIdInAndSquadId(ids, getSquadId());
    }

    public List<TopListsDTO> getTopRatedPlayersWithoutRecentGames() {
        Integer squadId = getSquadId();
        List<Object[]> topRatedPlayers = playerRepository.findTopRatedPlayers(squadId);
        List<TopListsDTO> topRatedList = topRatedPlayers.stream()
                .map(record -> new TopListsDTO(
                        (Integer) record[0],
                        (String) record[1],
                        (String) record[2],
                        (Double) record[3],
                        (Long) record[4]
                ))
                .toList();

        List<Integer> recentGamePlayerIds = playerRepository.findPlayersInRecentGames(squadId);

        return topRatedList.stream()
                .filter(player -> recentGamePlayerIds.contains(player.getPlayerId()))
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<TopListProjection> getLegendaryDuos() {
        Integer squadId = getSquadId();
        List<TopListProjection> rawDataLegendaryDuos = playerRepository.getLegendaryDuos(squadId);

        Set<Integer> usedPlayers = new HashSet<>();
        List<TopListProjection> filteredResults = new ArrayList<>();

        for (TopListProjection duo : rawDataLegendaryDuos) {
            Integer player1 = duo.getPlayer1Id();
            Integer player2 = duo.getPlayer2Id();

            if (usedPlayers.contains(player1) || usedPlayers.contains(player2)) {
                continue;
            }

            filteredResults.add(duo);
            usedPlayers.add(player1);
            usedPlayers.add(player2);

            if (filteredResults.size() >= 5) {
                break;
            }
        }

        return filteredResults;
    }

    public List<TopListProjection> getRivalDuos() {
        Integer squadId = getSquadId();
        List<TopListProjection> legendaryDuos = getLegendaryDuos();
        List<TopListProjection> rawDataRivalDuos = playerRepository.getRivalDuos(squadId);

        Set<Set<Integer>> legendaryPairs = new HashSet<>();
        for (TopListProjection duo : legendaryDuos) {
            Set<Integer> pair = new HashSet<>(Arrays.asList(duo.getPlayer1Id(), duo.getPlayer2Id()));
            legendaryPairs.add(pair);
        }

        List<TopListProjection> filteredResults = new ArrayList<>();
        Set<Integer> usedPlayers = new HashSet<>();

        for (TopListProjection duo : rawDataRivalDuos) {
            Set<Integer> pair = new HashSet<>(Arrays.asList(duo.getPlayer1Id(), duo.getPlayer2Id()));

            if (legendaryPairs.contains(pair)) {
                continue;
            }

            if (usedPlayers.contains(duo.getPlayer1Id()) || usedPlayers.contains(duo.getPlayer2Id())) {
                continue;
            }

            filteredResults.add(duo);
            usedPlayers.add(duo.getPlayer1Id());
            usedPlayers.add(duo.getPlayer2Id());

            if (filteredResults.size() >= 5) {
                break;
            }
        }

        return filteredResults;
    }

    @Cacheable(value = "playerCache")
    @Transactional
    public Map<Integer, PlayerDTO> findPlayersByIds(List<Integer> playerIds) {
        List<Player> players = playerRepository.findByIdIn(playerIds);

        return players.stream()
                .collect(Collectors.toMap(Player::getId, player -> {
                    PlayerDTO playerDTO = new PlayerDTO();
                    playerDTO.setId(player.getId());
                    playerDTO.setName(player.getName());
                    playerDTO.setSurname(player.getSurname());
                    return playerDTO;
                }));
    }
}
