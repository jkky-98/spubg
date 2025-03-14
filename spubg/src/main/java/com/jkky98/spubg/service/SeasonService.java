package com.jkky98.spubg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jkky98.spubg.domain.Season;
import com.jkky98.spubg.pubg.request.PubgApiManager;
import com.jkky98.spubg.repository.SeasonRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonService {
    private final SeasonRepository seasonRepository;
    private final PubgApiManager pubgApiManager;

    @Transactional
    public void updateSeason() {

        JsonNode response = pubgApiManager.requestSeason();

        for (JsonNode seasonNode : response.get("data")) {
            boolean isCurrent = seasonNode.get("attributes").get("isCurrentSeason").asBoolean();
            boolean isOffseason = seasonNode.get("attributes").get("isOffseason").asBoolean();
            String seasonId = seasonNode.get("id").asText();

            if (isCurrent) {  // 🔹 현재 시즌인 경우만 저장
                Season seasonNew = Season.builder()
                        .seasonApiId(seasonId)
                        .boolIsCurrentSeason(true)
                        .boolIsOffseason(isOffseason)
                        .build();

                // 현재 시즌과 DB 시즌 비교
                Season seasonOld = seasonRepository.findByBoolIsCurrentSeasonTrue().orElseThrow(EntityNotFoundException::new);

                if (seasonOld.getSeasonApiId().equals(seasonNew.getSeasonApiId())) {
                    return;
                } else {
                    seasonRepository.delete(seasonOld);
                    seasonRepository.save(seasonNew);
                }
            } else {
                return;
            }
        }
    }

    @Transactional(readOnly = true)
    public String getCurrentSeasonId() {
        return seasonRepository.findByBoolIsCurrentSeasonTrue().orElseThrow(EntityNotFoundException::new).getSeasonApiId();
    }
}
