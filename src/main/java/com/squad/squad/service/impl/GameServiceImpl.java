package com.squad.squad.service.impl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.squad.squad.dto.MvpDTO;
import com.squad.squad.dto.game.GameCreateRequestDTO;
import com.squad.squad.dto.game.GameResponseDTO;
import com.squad.squad.dto.game.GameUpdateRequestDTO;
import com.squad.squad.dto.goal.GoalResponseDTO;
import com.squad.squad.dto.roster.RosterCreateDTO;
import com.squad.squad.dto.roster.RosterResponseDTO;
import com.squad.squad.dto.roster.RosterUpdateDTO;
import com.squad.squad.entity.*;
import com.squad.squad.mapper.GameLocationMapper;
import com.squad.squad.mapper.GameMapper;
import com.squad.squad.mapper.GoalMapper;
import com.squad.squad.repository.GameLocationRepository;
import com.squad.squad.repository.RatingRepository;
import com.squad.squad.repository.RosterPersonaRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.squad.squad.dto.LatestGamesDTO;
import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.enums.TeamColor;
import com.squad.squad.exception.GameNotFoundException;
import com.squad.squad.exception.NotFoundException;

import com.squad.squad.mapper.PlayerMapper;
import com.squad.squad.repository.GameRepository;
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
    private final GameLocationMapper gameLocationMapper ;
    private final PlayerMapper playerMapper;
    private final RosterPersonaRepository rosterPersonaRepository;

    @Autowired
    public GameServiceImpl(GameRepository gameRepository, RosterService rosterService,
                           PlayerService playerService, RosterPersonaRepository rosterPersonaRepository
                           ,GameLocationRepository gameLocationRepository, RatingRepository ratingRepository, GoalMapper goalMapper, GameMapper gameMapper, GameLocationMapper gameLocationMapper, PlayerMapper playerMapper) {
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
    }

    @Override
    public GameResponseDTO getLatestGame() {

        checkAndUpdateUnplayedGame();

        return gameMapper.gameToGameResponseDTO(gameRepository.findTopByOrderByDateTimeDesc());
    }

    @Override
    public Page<LatestGamesDTO> getAllGames(Pageable pageable) {
        return gameRepository.findAllByOrderByDateTimeDesc(pageable).map(game ->
                new LatestGamesDTO(game.getId(), game.getDateTime(), game.getHomeTeamScore(),
                        game.getAwayTeamScore(), game.isPlayed()));
    }

    @Override
    public Game findGameById(Integer id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));
    }

    @Override
    public Page<LatestGamesDTO> getAllGames() {
        return null;
    }

    @Override
    public GameResponseDTO getGameById(Integer id) {

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));

        GameLocation gameLocation = gameLocationRepository.findById(game.getGameLocation().getId())
                .orElseThrow(() -> new NotFoundException("Game location not found with id: " + game.getGameLocation().getId()));


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
            throw new IllegalArgumentException("There is already a planned or not yet voted game.");
        }

        Game game = new Game();
        game.setDateTime(gameDto.getDateTime());
        game.setWeather(gameDto.getWeather());
        game.setLocation(gameDto.getLocation());


        GameLocation gameLocation = gameLocationRepository.findById(Integer.parseInt(gameDto.getLocation()))
                .orElseThrow(() -> new NotFoundException("Game location not found with id: " + gameDto.getGameLocationId()));

        game.setGameLocation(gameLocation);

        List<Roster> rosters = new ArrayList<>();

        for (RosterCreateDTO rosterDTO : gameDto.getRosters()) {
            Roster roster = new Roster();
            roster.setGame(game);
            roster.setTeamColor(rosterDTO.getTeamColor().toUpperCase());

            Player player = playerMapper.playerDTOToPlayer(playerService.getPlayerById(rosterDTO.getPlayerId()));

            if (player == null) {
                throw new RuntimeException("Player not found with id: " + rosterDTO.getPlayerId());
            }

            roster.setPlayer(player);

            rosters.add(roster);
        }

        game.setHomeTeamScore(0);
        game.setAwayTeamScore(0);
        Game savedGame = gameRepository.save(game);

        rosters.forEach(roster -> roster.setGame(savedGame));
        rosterService.saveAllRosters(rosters);

        // yeni mac olusturuldugu icin son mac icin rating tablosu sifirliyoruz data birikmesin diye
        ratingRepository.deleteAll();
        rosterPersonaRepository.deleteAll();
    }

    @Override
    @Transactional
    public void updateGame(Integer id, GameUpdateRequestDTO updatedGame) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));

        if (game.isPlayed()) {
            throw new IllegalArgumentException("Game has already been played. You cannot update game details.");
        }

        GameLocation gameLocation = gameLocationRepository.findById(Integer.parseInt(updatedGame.getLocation()))
                .orElseThrow(() -> new NotFoundException("Game location not found with id: " + updatedGame.getLocation()));

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
                            updateFieldIfNotNull(rosterUpdateDTO.getTeamColor().toUpperCase(), existingRoster::setTeamColor);

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

        Game existingGame = gameRepository.findById(goal.getGame().getId())
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
            gameRepository.deleteById(id);
        } catch (Exception e) {
            throw new NotFoundException("Game or roster not found with game id: " + id);
        }
    }

    @Override
    public Game findById(Integer id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));
    }

    @Override
    public void checkAndUpdateUnplayedGame() {
        Game unplayedGame = gameRepository.findByIsPlayedFalse();

        if (unplayedGame != null) {
            LocalDateTime currentTime = LocalDateTime.now();

            if (currentTime.isAfter(unplayedGame.getDateTime())) {
                unplayedGame.setPlayed(true);
                gameRepository.save(unplayedGame);
            }
        }
    }

    public List<Roster> getRostersByGameId(Integer gameId) {
        return gameRepository.findById(gameId)
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
        // Son oynanan maçtaki tüm oyuncuları alıyoruz
        List<MvpDTO> players = gameRepository.findMvpPlayers();

        // Listeden en yüksek rating'e sahip oyuncuyu bulmak için stream kullanıyoruz
        return players.stream()
                .max(Comparator.comparingDouble(MvpDTO::getRating));
    }

    @Override
    public void updateWeather(Integer id,String weather) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));

        // Fazladan tırnakları kaldır
        String cleanedWeather = weather.replace("\"", "").trim();

        if (StringUtils.isNotBlank(game.getWeather())) {
            throw new IllegalArgumentException("Game has already weather info. You cannot update weather.");
        }

        updateFieldIfNotNull(cleanedWeather, game::setWeather);

        gameRepository.save(game);

    }
}