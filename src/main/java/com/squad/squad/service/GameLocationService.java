package com.squad.squad.service;

import com.squad.squad.dto.GameLocationDTO;

import java.util.List;

public interface GameLocationService {

    List<GameLocationDTO> getAll();

    GameLocationDTO create(GameLocationDTO dto);

    GameLocationDTO update(Integer id, GameLocationDTO dto);

    void delete(Integer id);
}
