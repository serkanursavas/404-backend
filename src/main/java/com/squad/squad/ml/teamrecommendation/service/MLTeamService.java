package com.squad.squad.ml.teamrecommendation.service;

import com.squad.squad.ml.teamrecommendation.dto.MLPlayerDTO;
import com.squad.squad.ml.teamrecommendation.dto.MLTeamRequestDTO;
import com.squad.squad.ml.teamrecommendation.dto.MLTeamResponseDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import org.springframework.beans.factory.annotation.Value;


import java.util.List;

@Service
public class MLTeamService {

    private final RestTemplate restTemplate;
    private final String mlServiceUrl;

    public MLTeamService(RestTemplate restTemplate,
                         @Value("${ml.service.url}") String mlServiceUrl) {
        this.restTemplate = restTemplate;
        this.mlServiceUrl = mlServiceUrl;
    }

    /**
     * Seçilen oyunculardan dengeli iki takım tahmini yapar.
     */
    public MLTeamResponseDTO predictTeams(List<MLPlayerDTO> players) {
        MLTeamRequestDTO payload = new MLTeamRequestDTO(players);

        // ① Ham cevabı String olarak al
        ResponseEntity<String> raw = restTemplate.postForEntity(
                mlServiceUrl,
                new HttpEntity<>(payload),
                String.class
        );

        // ② Konsola bas
        System.out.println(">>> ML raw response JSON:\n" + raw.getBody());

        // ③ Normal mapping’e devam et (isterseniz ObjectMapper ile de map edebilirsiniz)
        MLTeamResponseDTO mapped = restTemplate.postForObject(
                mlServiceUrl,
                payload,
                MLTeamResponseDTO.class
        );

        return mapped;
    }
}