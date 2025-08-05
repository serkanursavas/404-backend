package com.squad.squad.service.impl;

import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.dto.PlayerPersonaDTO;
import com.squad.squad.dto.TopListProjection;
import com.squad.squad.dto.TopListsDTO;
import com.squad.squad.dto.player.GetAllActivePlayersDTO;
import com.squad.squad.dto.player.PlayerUpdateRequestDTO;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.User;
import com.squad.squad.exception.PlayerNotFoundException;
import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.repository.PlayerRepository;
import com.squad.squad.repository.UserRepository;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.service.PlayerService;
import com.squad.squad.security.JwtGroupContextService;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final UserRepository userRepository;
    private final JwtGroupContextService jwtGroupContextService;

    public PlayerServiceImpl(PlayerRepository playerRepository, PlayerMapper playerMapper,
            UserRepository userRepository, JwtGroupContextService jwtGroupContextService) {
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
        this.userRepository = userRepository;
        this.jwtGroupContextService = jwtGroupContextService;
    }

    @Override
    public List<PlayerDTO> getAllPlayers() {
        CustomUserDetails currentUser = getCurrentUser();

        List<Player> players;

        // Super Admin ise tüm player'ları göster
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            // Super admin için tüm player'ları göster (güvenlik kontrolü bypass)
            players = playerRepository.findByActiveTrue(); // Güvenli method kullan
        } else {
            // Normal kullanıcı ise SecureJpaRepository otomatik filtreleme yapar
            Integer currentGroupId = jwtGroupContextService.getCurrentApprovedGroupId();
            Integer currentUserId = jwtGroupContextService.getCurrentUserId();
            players = playerRepository.findAllByCurrentGroup(currentGroupId, currentUserId); // Current group
                                                                                             // context'ten alır
        }

        Collator trCollator = Collator.getInstance(new Locale("tr", "TR"));

        players.sort(Comparator
                .comparing(Player::getName, trCollator)
                .thenComparing(Player::getSurname, trCollator));

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
                            .collect(Collectors.toList());

                    playerDTO.setPersonas(personas);
                    return playerDTO;
                })
                .collect(Collectors.toList());
    }

    // Helper method
    private CustomUserDetails getCurrentUser() {
        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public PlayerDTO getPlayerById(Integer id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException("User not found with id: " + id));

        Integer currentGroupId = jwtGroupContextService.getCurrentApprovedGroupId();
        Integer currentUserId = jwtGroupContextService.getCurrentUserId();
        Player player = playerRepository
                .findByIdAndCurrentGroup(user.getPlayer().getId(), currentGroupId, currentUserId)
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

        List<Double> last5GameRating = playerRepository.getLast5MatchRatingByPlayerId(player.getId(), currentGroupId);

        playerDTO.setLast5GameRating(last5GameRating);

        return playerDTO;
    }

    @Override
    public void updatePlayer(PlayerUpdateRequestDTO updatedPlayer) {
        Integer currentGroupId = jwtGroupContextService.getCurrentApprovedGroupId();
        Integer currentUserId = jwtGroupContextService.getCurrentUserId();
        Player existingPlayer = playerRepository
                .findByIdAndCurrentGroup(updatedPlayer.getId(), currentGroupId, currentUserId)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + updatedPlayer.getId()));

        BeanUtils.copyProperties(updatedPlayer, existingPlayer);

        playerRepository.save(existingPlayer);
    }

    @Override
    public void softDelete(PlayerDTO deletedPlayer) {
        Integer currentGroupId = jwtGroupContextService.getCurrentApprovedGroupId();
        Integer currentUserId = jwtGroupContextService.getCurrentUserId();
        Player existingPlayer = playerRepository
                .findByIdAndCurrentGroup(deletedPlayer.getId(), currentGroupId, currentUserId)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + deletedPlayer.getId()));

        existingPlayer.setActive(false);
        playerRepository.save(existingPlayer);
    }

    @Override
    public List<GetAllActivePlayersDTO> getAllActivePlayers() {
        return playerMapper.playersToGetAllActivePlayersDTOs(playerRepository.findByActiveTrue());
    }

    @Override
    public List<Player> findAllById(List<Integer> playerIds) {
        Integer currentGroupId = jwtGroupContextService.getCurrentApprovedGroupId();
        Integer currentUserId = jwtGroupContextService.getCurrentUserId();
        return playerRepository.findAllByIdAndCurrentGroup(playerIds, currentGroupId, currentUserId);
    }

    public List<TopListsDTO> getTopRatedPlayersWithoutRecentGames() {
        // 1. Top Rated oyuncuları al
        Integer currentGroupId = jwtGroupContextService.getCurrentApprovedGroupId();
        List<Object[]> topRatedPlayers = playerRepository.findTopRatedPlayers(currentGroupId);
        List<TopListsDTO> topRatedList = topRatedPlayers.stream()
                .map(record -> new TopListsDTO(
                        (Integer) record[0], // playerId
                        (String) record[1], // name
                        (String) record[2], // surname
                        (Double) record[3], // rating
                        (Long) record[4] // rating
                ))
                .toList();

        // 2. Son 2 maçtaki oyuncuları al
        List<Integer> recentGamePlayerIds = playerRepository.findPlayersInRecentGames(currentGroupId);

        // 3. Filtreleme
        return topRatedList.stream()
                .filter(player -> recentGamePlayerIds.contains(player.getPlayerId()))
                .limit(10) // En iyi 5 oyuncuyu al
                .collect(Collectors.toList());
    }

    public List<TopListProjection> getLegendaryDuos() {
        Integer currentGroupId = jwtGroupContextService.getCurrentApprovedGroupId();
        List<TopListProjection> rawDataLegendaryDuos = playerRepository.getLegendaryDuos(currentGroupId);

        Set<Integer> usedPlayers = new HashSet<>();
        List<TopListProjection> filteredResults = new ArrayList<>();

        for (TopListProjection duo : rawDataLegendaryDuos) {
            Integer player1 = duo.getPlayer1Id();
            Integer player2 = duo.getPlayer2Id();

            // Eğer oyunculardan biri daha önce listede varsa, atla
            if (usedPlayers.contains(player1) || usedPlayers.contains(player2)) {
                continue;
            }

            // Çifti listeye ekle ve oyuncuları işaretle
            filteredResults.add(duo);
            usedPlayers.add(player1);
            usedPlayers.add(player2);

            // İlk 5 sonucu aldıktan sonra döngüyü durdur
            if (filteredResults.size() >= 5) {
                break;
            }
        }

        return filteredResults;
    }

    public List<TopListProjection> getRivalDuos() {
        // 1️⃣ "Legendary Duos" verisini servis üzerinden al
        List<TopListProjection> legendaryDuos = getLegendaryDuos();

        // 2️⃣ "Rival Duos" verisini repodan al
        Integer currentGroupId = jwtGroupContextService.getCurrentApprovedGroupId();
        List<TopListProjection> rawDataRivalDuos = playerRepository.getRivalDuos(currentGroupId);

        // 3️⃣ "Legendary Duos" içindeki oyuncu çiftlerini sakla
        Set<Set<Integer>> legendaryPairs = new HashSet<>();

        for (TopListProjection duo : legendaryDuos) {
            Set<Integer> pair = new HashSet<>(Arrays.asList(duo.getPlayer1Id(), duo.getPlayer2Id()));
            legendaryPairs.add(pair);
        }

        // 4️⃣ "Rival Duos" listesinden "Legendary Duos" içindeki çiftleri çıkar
        List<TopListProjection> filteredResults = new ArrayList<>();

        // 5️⃣ Daha önce listeye eklenmiş oyuncuları takip etmek için bir set
        // oluşturuyoruz
        Set<Integer> usedPlayers = new HashSet<>();

        for (TopListProjection duo : rawDataRivalDuos) {
            Set<Integer> pair = new HashSet<>(Arrays.asList(duo.getPlayer1Id(), duo.getPlayer2Id()));

            // Eğer bu çift "Legendary Duos" içinde varsa, listeye ekleme
            if (legendaryPairs.contains(pair)) {
                continue;
            }

            // Eğer bu oyunculardan biri zaten başka bir çiftte listede varsa, ekleme
            if (usedPlayers.contains(duo.getPlayer1Id()) || usedPlayers.contains(duo.getPlayer2Id())) {
                continue;
            }

            // Çifti listeye ekle ve oyuncuları işaretle
            filteredResults.add(duo);
            usedPlayers.add(duo.getPlayer1Id());
            usedPlayers.add(duo.getPlayer2Id());

            // İlk 5 sonucu aldıktan sonra döngüyü durdur
            if (filteredResults.size() >= 5) {
                break;
            }
        }

        return filteredResults;
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