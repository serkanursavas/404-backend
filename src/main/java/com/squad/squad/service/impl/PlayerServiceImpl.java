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
import com.squad.squad.repository.PersonaRepository;
import com.squad.squad.repository.PlayerPersonaRepository;
import com.squad.squad.repository.PlayerRepository;
import com.squad.squad.repository.UserRepository;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.service.PlayerService;
import com.squad.squad.service.TenantContextService;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final TenantContextService tenantContextService;
    private final UserRepository userRepository;

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository, PlayerMapper playerMapper,  TenantContextService tenantContextService, UserRepository userRepository) {
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
        this.tenantContextService = tenantContextService;
        this.userRepository = userRepository;
    }

    @Override
    public List<PlayerDTO> getAllPlayers() {
        CustomUserDetails currentUser = getCurrentUser();

        List<Player> players;

        // Super Admin ise tüm player'ları göster
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            players = playerRepository.findAll();
        } else {
            // Normal kullanıcı ise sadece kendi grubundaki player'ları göster
            User user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı."));

            Integer userGroupId = user.getGroupId();

            if (userGroupId == 0) {
                // Kullanıcı henüz onay bekliyor, sadece kendi player'ını göster
                if (user.getPlayer().getId() != null) {
                    players = playerRepository.findByPlayerId(user.getPlayer().getId());
                } else {
                    players = new ArrayList<>(); // Player'ı yoksa boş liste
                }
                System.out.println("User in pending state, showing only own player: " + players.size());
            } else {
                // Onaylanmış kullanıcı, grubundaki tüm player'ları göster
                players = playerRepository.findByGroupId(userGroupId);
                System.out.println("User in approved group " + userGroupId + ", showing " + players.size() + " players");
            }
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
                            .toList();

                    playerDTO.setPersonas(personas);

                    List<Double> last5GameRating = playerRepository.getLast5MatchRatingByPlayerId(player.getId());
                    playerDTO.setLast5GameRating(last5GameRating);

                    return playerDTO;
                })
                .toList();
    }

    // Helper method
    private CustomUserDetails getCurrentUser() {
        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public PlayerDTO getPlayerById(Integer id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException("User not found with id: " + id));

        Player player = playerRepository.findById(user.getPlayer().getId())
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

        List<Double> last5GameRating = playerRepository.getLast5MatchRatingByPlayerId(player.getId());

        playerDTO.setLast5GameRating(last5GameRating);


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
                        (Double) record[3],      // rating
                        (Long) record[4]      // rating
                ))
                .toList();

        // 2. Son 2 maçtaki oyuncuları al
        List<Integer> recentGamePlayerIds = playerRepository.findPlayersInRecentGames();

        // 3. Filtreleme
        return topRatedList.stream()
                .filter(player -> recentGamePlayerIds.contains(player.getPlayerId()))
                .limit(10)  // En iyi 5 oyuncuyu al
                .collect(Collectors.toList());
    }

    public List<TopListProjection> getLegendaryDuos() {
        List<TopListProjection> rawDataLegendaryDuos = playerRepository.getLegendaryDuos();

        List<TopListProjection> rawDataRivalDuos = playerRepository.getRivalDuos();

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
        List<TopListProjection> rawDataRivalDuos = playerRepository.getRivalDuos();

        // 3️⃣ "Legendary Duos" içindeki oyuncu çiftlerini sakla
        Set<Set<Integer>> legendaryPairs = new HashSet<>();

        for (TopListProjection duo : legendaryDuos) {
            Set<Integer> pair = new HashSet<>(Arrays.asList(duo.getPlayer1Id(), duo.getPlayer2Id()));
            legendaryPairs.add(pair);
        }

        // 4️⃣ "Rival Duos" listesinden "Legendary Duos" içindeki çiftleri çıkar
        List<TopListProjection> filteredResults = new ArrayList<>();

        // 5️⃣ Daha önce listeye eklenmiş oyuncuları takip etmek için bir set oluşturuyoruz
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