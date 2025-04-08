package com.jkky98.spubg.service.business;

import com.fasterxml.jackson.databind.JsonNode;
import com.jkky98.spubg.pubg.enums.GameMode;
import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.Season;
import com.jkky98.spubg.pubg.enums.GameMap;
import com.jkky98.spubg.service.business.exception.MatchSyncValidationException;
import com.jkky98.spubg.service.implement.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchSyncService {

    private final MatchReader matchReader;
    private final MemberMatchSyncService memberMatchSyncService;
    private final SeasonReader seasonReader;

    /**
     * 매치 데이터를 통해 멤버-매치 데이터 추가 및 누락 매치 데이터 보완
     * 매치 엔티티는 이미 DB에 존재하는 match를 받게 된다.
     * @param matchId
     * @param rootNode
     */
    @Transactional
    public void sync(Long matchId, JsonNode rootNode) {
        Match matchRead = matchReader.read(matchId);

        try {
            validMatchSync(rootNode);

            /**
             *  등록된 멤버에 대해 매치 데이터 안에 멤버들 누락 존재할 경우 멤버-매치에 추가
             */
            memberMatchSyncService.syncMemberMatchIfMissing(rootNode, matchRead);
            Season currentSeason = seasonReader.readCurrentSeason();
            syncMatch(rootNode, matchRead, currentSeason.getSeasonApiId());
        } catch (MatchSyncValidationException e) {
            log.warn(e.getMessage());
            garbageSyncMatch(matchRead);
        }
    }

    private static void syncMatch(JsonNode rootNode, Match matchRead, String currentSeasonId) {
        JsonNode attNode = rootNode.path("data").path("attributes");

        String mapName = GameMap.getDisplayName(getMapNameFromAttNode(attNode));
        String assetId = getAssetId(rootNode);
        String assetURL = getAssetURL(rootNode, assetId);
        LocalDateTime createdAt = getCreatedAt(attNode);

        updateEntity(
                matchRead,
                mapName,
                assetId,
                assetURL,
                createdAt,
                currentSeasonId
        );
        log.debug("[MatchSyncService][sync][syncMatch] 정상적으로 매치 정보가 업데이트 되었습니다. : 분석이 필요한 매치");
    }

    private static void updateEntity(Match matchRead, String mapName, String assetId, String assetURL, LocalDateTime createdAt, String currentSeasonId) {
        matchRead.setBoolIsAnalysis(true);
        matchRead.setMap(mapName);
        matchRead.setAssetId(assetId);
        matchRead.setAssetUrl(assetURL);
        matchRead.setCreatedAt(createdAt);
        matchRead.setGameMode(GameMode.SQUAD);
        matchRead.setSeason(currentSeasonId);
    }

    private static String getAssetId(JsonNode rootNode) {
        JsonNode assetsArrayNode = rootNode.path("data")
                .path("relationships")
                .path("assets")
                .path("data");

        if (assetsArrayNode.isArray() && !assetsArrayNode.isEmpty()) {
            for (JsonNode assetNode : assetsArrayNode) {
                if ("asset".equals(assetNode.path("type").asText())) {
                    String assetId = assetNode.path("id").asText();
                    if (!assetId.isEmpty()) {
                        return assetId;
                    }
                }
            }
        }

        throw new MatchSyncValidationException("assetId를 찾을 수 없습니다.");
    }


    private static String getAssetURL(JsonNode rootNode, String assetId) {
        JsonNode includedNodes = rootNode.path("included");
        if (includedNodes.isArray()) {
            for (JsonNode node : includedNodes) {
                if ("asset".equals(node.path("type").asText())
                        && assetId.equals(node.path("id").asText())) {
                    String assetUrl = node.path("attributes").path("URL").asText();
                    if (!assetUrl.isEmpty()) {
                        return assetUrl;
                    }
                }
            }
        }
        throw new MatchSyncValidationException("assetUrl이 존재하지 않습니다.");
    }

    private static LocalDateTime getCreatedAt(JsonNode attNode) {
        String createdAtString = Optional.ofNullable(attNode.get("createdAt"))
                .map(JsonNode::asText)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new MatchSyncValidationException("createdAt 필드가 없거나 비어있습니다."));

        try {
            return Instant.parse(createdAtString)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toLocalDateTime();
        } catch (DateTimeParseException e) {
            throw new MatchSyncValidationException("createdAt의 형식이 올바르지 않습니다.");
        }
    }

    private static String getMapNameFromAttNode(JsonNode attNode) {
        String mapNameRaw = attNode.path("mapName").asText();
        if (mapNameRaw.isEmpty()) {
            throw new MatchSyncValidationException("mapName이 attNode에 존재하지 않습니다.");
        }
        return mapNameRaw;
    }

    private void validMatchSync(JsonNode rootNode) {
        JsonNode dataNode = rootNode.path("data");

        if (dataNode.isMissingNode()) {
            // 최상위 errors 배열을 직접 확인
            JsonNode errorsNode = rootNode.path("errors");
            String errorDetail = null;
            if (errorsNode.isArray() && !errorsNode.isEmpty()) {
                errorDetail = errorsNode.get(0).path("detail").asText(null);
            }

            throw new MatchSyncValidationException(errorDetail != null
                    ? "매치 json 데이터를 읽을 수 없습니다. error Detail : " + errorDetail
                    : "매치 json 데이터를 읽을 수 없습니다.");
        }

        if (!"match".equals(dataNode.path("type").asText())) {
            throw new MatchSyncValidationException("서버에서 정의한 올바른 매치 타입이 아닙니다.");
        }

        JsonNode attNode = dataNode.path("attributes");

        if (attNode.isMissingNode()) {
            throw new MatchSyncValidationException("data노드는 존재하지만, attributes 노드가 존재하지 않습니다.");
        }

        if (attNode.path("isCustomMatch").asBoolean()) {
            throw new MatchSyncValidationException("이 매치는 커스텀 매치이므로 분석 대상이 아닙니다.");
        }

        if (!"official".equals(attNode.path("matchType").asText())) {
            throw new MatchSyncValidationException("오피셜 타입의 매치가 아니므로 분석 대상이 아닙니다.");
        }

        if (!"squad".equals(attNode.path("gameMode").asText())) {
            throw new MatchSyncValidationException("게임 모드가 스쿼드가 아니므로 분석 대상이 아닙니다.");
        }

        if (rootNode.path("included").isMissingNode()) {
            throw new MatchSyncValidationException("included 노드가 존재하지 않습니다.");
        }
    }

    private static void garbageSyncMatch(Match matchRead) {
        // 매치 엔티티를 분석 과정이 없었으나 분석 완료로 처리해버림.
        // 이유 : 위 예외를 거친 매치 엔티티는 분석필요 없음.
        // 분석 완료된 매치는 분석 대상에 포함되지 않음.
        matchRead.setBoolIsAnalysis(true);
        matchRead.setGameMode(GameMode.OTHER);
        log.debug("[MatchSyncService][sync][garbageSyncMatch] 정상적으로 매치 정보가 업데이트 되었습니다. : 예외 매치");
    }
}
