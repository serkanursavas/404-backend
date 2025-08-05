package com.squad.squad.service.impl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.squad.squad.dto.LatestGamesDTO;
import com.squad.squad.dto.MvpDTO;
import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.dto.game.GameCreateRequestDTO;
import com.squad.squad.dto.game.GameResponseDTO;
import com.squad.squad.dto.game.GameUpdateRequestDTO;
import com.squad.squad.dto.goal.GoalResponseDTO;
import com.squad.squad.dto.roster.RosterCreateDTO;
import com.squad.squad.dto.roster.RosterResponseDTO;
import com.squad.squad.dto.roster.RosterUpdateDTO;
import com.squad.squad.entity.*;
import com.squad.squad.enums.TeamColor;
import com.squad.squad.exception.GameNotFoundException;
import com.squad.squad.exception.NotFoundException;
import com.squad.squad.mapper.GameLocationMapper;
import com.squad.squad.mapper.GameMapper;
import com.squad.squad.mapper.GoalMapper;
import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.repository.GameLocationRepository;
import com.squad.squad.repository.GameRepository;
import com.squad.squad.repository.RatingRepository;
import com.squad.squad.repository.RosterPersonaRepository;
import com.squad.squad.security.JwtGroupContextService;
import com.squad.squad.service.GameService;
import com.squad.squad.service.PlayerService;
import com.squad.squad.service.RosterService;

import jakarta.transaction.Transactional;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final RosterService rosterService;
    private final PlayerService playerService;
    private final GameLocationRepository gameLocationRepository;
    private final RatingRepository ratingRepository;
    private final GoalMapper goalMapper;
    private final GameMapper gameMapper;
    private final GameLocationMapper gameLocationMapper;
    private final PlayerMapper playerMapper;
    private final RosterPersonaRepository rosterPersonaRepository;
    private final JwtGroupContextService jwtGroupContextService;

    public GameServiceImpl(GameRepository gameRepository, RosterService rosterService,
            PlayerService playerService, RosterPersonaRepository rosterPersonaRepository,
            GameLocationRepository gameLocationRepository, RatingRepository ratingRepository, GoalMapper goalMapper,
            GameMapper gameMapper, GameLocationMapper gameLocationMapper, PlayerMapper playerMapper,
            JwtGroupContextService jwtGroupContextService) {
        this.gameRepository = gameRepository;
        this.rosterService = rosterService;
        this.playerService = playerService;
        this.gameLocationRepository = gameLocationRepository;
        this.ratingRepository = ratingRepository;
        this.goalMapper = goalMapper;
        this.gameMapper = gameMapper;
        this.gameLocationMapper = gameLocationMapper;
        this.playerMapper = playerMapper;
        this.rosterPersonaRepository = rosterPersonaRepository;
        this.jwtGroupContextService = jwtGroupContextService;
    }

    @Override
    public GameResponseDTO getLatestGame() {
        checkAndUpdateUnplayedGame();

        Game latestGame = gameRepository
                .findTopByOrderByDateTimeDesc(jwtGroupContextService.getCurrentApprovedGroupId());

        return gameMapper.gameToGameResponseDTO(latestGame);
    }

    @Override
    public Page<LatestGamesDTO> getAllGames(Pageable pageable) {
        return gameRepository.findAllByOrderByDateTimeDesc(pageable)
                .map(game -> new LatestGamesDTO(game.getId(), game.getDateTime(), game.getHomeTeamScore(),
                        game.getAwayTeamScore(), game.isPlayed()));
    }

    @Override
    public Game findGameById(Integer id) {
        return gameRepository
                .findByIdAndCurrentGroup(id, jwtGroupContextService.getCurrentApprovedGroupId(),
                        jwtGroupContextService.getCurrentUserId())
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));
    }

    @Override
    public Page<LatestGamesDTO> getAllGames() {
        return null;
    }

    @Override
    public GameResponseDTO getGameById(Integer id) {

        Game game = gameRepository
                .findByIdAndCurrentGroup(id, jwtGroupContextService.getCurrentApprovedGroupId(),
                        jwtGroupContextService.getCurrentUserId())
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));

        GameLocation gameLocation = gameLocationRepository.findByIdAndGroupId(game.getGameLocation().getId(),
                jwtGroupContextService.getCurrentApprovedGroupId())
                .orElseThrow(() -> new NotFoundException(
                        "Game location not found with id: " + game.getGameLocation().getId()));

        List<RosterResponseDTO> rosters = rosterService.findRosterByGameId(id);
        List<GoalResponseDTO> goals = goalMapper.goalsToGoalResponseDTOs(game.getGoal());

        // 1. Roster ve Goal içerisindeki tüm playerId'leri toplayın
        Set<Integer> playerIds = new HashSet<>();
        rosters.forEach(roster -> playerIds.add(roster.getPlayerId()));
        goals.forEach(goal -> playerIds.add(goal.getPlayerId()));

        // 2. Tüm player bilgilerini bir defada çekin
        Map<Integer, PlayerDTO> playerMap = playerService.findPlayersByIds(new ArrayList<>(playerIds));

        // 3. Kadro ve goller için oyuncu bilgilerini set edin
        rosters.forEach(roster -> {
            PlayerDTO playerDto = playerMap.get(roster.getPlayerId());
            roster.setPlayerName(playerDto.getName() + " " + playerDto.getSurname());
        });

        goals.forEach(goal -> {
            PlayerDTO playerDto = playerMap.get(goal.getPlayerId());
            goal.setPlayerName(playerDto.getName() + " " + playerDto.getSurname());
        });

        // GameResponseDTO'yu oluşturun
        GameResponseDTO gameDTO = new GameResponseDTO();
        BeanUtils.copyProperties(game, gameDTO);
        gameDTO.setRosters(rosters);
        gameDTO.setGoals(goals);
        gameDTO.setGameLocation(gameLocationMapper.gameLocationToGameLocationDTO(gameLocation));
        return gameDTO;
    }

    @Override
    @Transactional
    public void createGame(GameCreateRequestDTO gameDto) {

        if (gameRepository.existsByIsPlayedFalseOrIsVotedFalse()) {
            throw new IllegalArgumentException(
                    "Zaten planlanmış veya henüz oylanmamış bir maç bulunmaktadır. Yeni maç oluşturmadan önce mevcut maçı tamamlayın.");
        }

        Game game = new Game();
        game.setDateTime(gameDto.getDateTime());
        game.setWeather(gameDto.getWeather());
        game.setLocation(gameDto.getLocation());
        game.setGroupId(jwtGroupContextService.getCurrentApprovedGroupId());

        GameLocation gameLocation = gameLocationRepository.findByIdAndGroupId(Integer.parseInt(gameDto.getLocation()),
                jwtGroupContextService.getCurrentApprovedGroupId())
                .orElseThrow(
                        () -> new NotFoundException("Maç konumu bulunamadı. ID: " + gameDto.getGameLocationId()));

        game.setGameLocation(gameLocation);

        List<Roster> rosters = new ArrayList<>();

        for (RosterCreateDTO rosterDTO : gameDto.getRosters()) {
            Roster roster = new Roster();
            roster.setGame(game);
            roster.setTeamColor(rosterDTO.getTeamColor().toUpperCase());
            roster.setGroupId(jwtGroupContextService.getCurrentApprovedGroupId());

            Player player = playerMapper.playerDTOToPlayer(playerService.getPlayerById(rosterDTO.getPlayerId()));

            if (player == null) {
                throw new RuntimeException("Oyuncu bulunamadı. ID: " + rosterDTO.getPlayerId());
            }

            roster.setPlayer(player);

            rosters.add(roster);
        }

        game.setHomeTeamScore(0);
        game.setAwayTeamScore(0);
        Game savedGame = gameRepository.save(game);

        rosters.forEach(roster -> roster.setGame(savedGame));
        rosterService.saveAllRosters(rosters);

        // yeni mac olusturuldugu icin son mac icin rating tablosu sifirliyoruz data
        // birikmesin diye
        ratingRepository.deleteAllByGroupId(jwtGroupContextService.getCurrentApprovedGroupId());
    }

    @Override
    @Transactional
    public void updateGame(Integer id, GameUpdateRequestDTO updatedGame) {
        Game game = gameRepository.findByIdAndGroupId(id, jwtGroupContextService.getCurrentApprovedGroupId())
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));

        if (game.isPlayed()) {
            throw new IllegalArgumentException("Bu maç zaten oynanmış. Maç detaylarını güncelleyemezsiniz.");
        }

        GameLocation gameLocation = gameLocationRepository
                .findByIdAndGroupId(Integer.parseInt(updatedGame.getLocation()),
                        jwtGroupContextService.getCurrentApprovedGroupId())
                .orElseThrow(
                        () -> new NotFoundException("Game location not found with id: " + updatedGame.getLocation()));

        game.setGameLocation(gameLocation);
        updateFieldIfNotNull(updatedGame.getDateTime(), game::setDateTime);

        gameRepository.save(game);

        if (updatedGame.getRosters() != null && !updatedGame.getRosters().isEmpty()) {

            List<Integer> rosterIds = updatedGame.getRosters().stream()
                    .map(RosterUpdateDTO::getId)
                    .collect(Collectors.toList());

            List<Roster> existingRosters = rosterService.findAllById(rosterIds);

            List<Integer> playerIds = updatedGame.getRosters().stream()
                    .map(RosterUpdateDTO::getPlayerId)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Integer, Player> playerMap = playerService.findAllById(playerIds).stream()
                    .collect(Collectors.toMap(Player::getId, player -> player));

            existingRosters.forEach(existingRoster -> {
                updatedGame.getRosters().stream()
                        .filter(rosterUpdateDTO -> rosterUpdateDTO.getId().equals(existingRoster.getId()))
                        .findFirst()
                        .ifPresent(rosterUpdateDTO -> {
                            updateFieldIfNotNull(rosterUpdateDTO.getTeamColor().toUpperCase(),
                                    existingRoster::setTeamColor);

                            Player player = playerMap.get(rosterUpdateDTO.getPlayerId());
                            if (player != null) {
                                existingRoster.setPlayer(player);
                            }
                        });
            });

            rosterService.updateAllRosters(existingRosters);
        }
    }

    @Override
    @Transactional
    public void updateScoreWithGoal(Goal goal) {

        Game existingGame = gameRepository.findByIdAndGroupId(goal.getGame().getId(),
                jwtGroupContextService.getCurrentApprovedGroupId())
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + goal.getGame().getId()));

        TeamColor teamColor = TeamColor.fromString(goal.getTeamColor());

        if (teamColor == TeamColor.BLACK) {
            existingGame.setHomeTeamScore(existingGame.getHomeTeamScore() + 1);
        } else {
            existingGame.setAwayTeamScore(existingGame.getAwayTeamScore() + 1);
        }

        existingGame.setPlayed(true);
        gameRepository.save(existingGame);
    }

    @Override
    @Transactional
    public void deleteGame(Integer id) {
        try {
            rosterService.deleteRosterByGameId(id);
            gameRepository.deleteByIdAndGroupId(id, jwtGroupContextService.getCurrentApprovedGroupId());
        } catch (Exception e) {
            throw new NotFoundException("Maç veya roster bulunamadı. Game ID: " + id);
        }
    }

    @Override
    public Game findById(Integer id) {
        return gameRepository
                .findByIdAndCurrentGroup(id, jwtGroupContextService.getCurrentApprovedGroupId(),
                        jwtGroupContextService.getCurrentUserId())
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));
    }

    @Override
    public void checkAndUpdateUnplayedGame() {
        Game unplayedGame = gameRepository
                .findByIsPlayedFalseAndGroupId(jwtGroupContextService.getCurrentApprovedGroupId());

        if (unplayedGame != null) {
            LocalDateTime currentTime = LocalDateTime.now();

            if (currentTime.isAfter(unplayedGame.getDateTime())) {
                unplayedGame.setPlayed(true);
                gameRepository.save(unplayedGame);
            }
        }
    }

    public List<Roster> getRostersByGameId(Integer gameId) {
        return gameRepository.findByIdAndGroupId(gameId, jwtGroupContextService.getCurrentApprovedGroupId())
                .map(Game::getRoster)
                .orElse(new ArrayList<>());
    }

    @Override
    public void updateVote(Game game) {
        gameRepository.save(game);
    }

    private <T> void updateFieldIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    @Override
    public Optional<MvpDTO> getMvpPlayer() {
        List<Object[]> result = gameRepository
                .findLatestVotedMvpRaw(jwtGroupContextService.getCurrentApprovedGroupId());

        if (result.isEmpty()) {
            return Optional.empty();
        }

        Object[] row = result.get(0);
        MvpDTO dto = new MvpDTO(
                (Integer) row[0], // id
                (String) row[1], // name
                (String) row[2], // surname
                (String) row[3], // photo
                (String) row[4], // position
                ((Number) row[5]).doubleValue() // rating (cast safe)
        );

        return Optional.of(dto);
    }

    @Override
    public void updateWeather(Integer id, String weather) {
        Game game = gameRepository.findByIdAndGroupId(id, jwtGroupContextService.getCurrentApprovedGroupId())
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));

        // Fazladan tırnakları kaldır
        String cleanedWeather = weather.replace("\"", "").trim();

        if (StringUtils.isNotBlank(game.getWeather())) {
            throw new IllegalArgumentException(
                    "Bu maçın zaten hava durumu bilgisi var. Hava durumunu güncelleyemezsiniz.");
        }

        updateFieldIfNotNull(cleanedWeather, game::setWeather);

        gameRepository.save(game);

    }
}