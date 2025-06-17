package com.squad.squad.ml.teamrecommendation.controller;

import com.squad.squad.ml.teamrecommendation.dto.MLPlayerDTO;
import com.squad.squad.ml.teamrecommendation.dto.MLTeamResponseDTO;
import com.squad.squad.ml.teamrecommendation.dto.PredictTeamsRequest;
import com.squad.squad.ml.teamrecommendation.service.MLDataPreparationService;
import com.squad.squad.ml.teamrecommendation.service.MLTeamService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ml")
public class MLTeamController {

    private final MLDataPreparationService prepService;
    private final MLTeamService mlTeamService;

    public MLTeamController(MLDataPreparationService prepService,
                            MLTeamService mlTeamService) {
        this.prepService   = prepService;
        this.mlTeamService = mlTeamService;
    }

    @PostMapping("/predict-teams")
    public MLTeamResponseDTO predictTeams(@RequestBody PredictTeamsRequest req) {
        // 1️⃣ Listeyi Integer’a çeviriyoruz
        List<Integer> ids = req.getPlayerIds().stream()
                .map(Long::intValue)
                .collect(Collectors.toList());

        // 2️⃣ MLPlayerDTO listesini hazırlıyoruz
        List<MLPlayerDTO> mlDtos = prepService.buildMlDtos(ids);

        // 3️⃣ Flask’a sorup sonucu döndürüyoruz
        return mlTeamService.predictTeams(mlDtos);
    }
}