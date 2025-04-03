package com.jkky98.spubg.service.business;

import com.fasterxml.jackson.databind.JsonNode;
import com.jkky98.spubg.domain.GameMode;
import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.pubg.request.PubgApiRequestService;
import com.jkky98.spubg.service.GameMap;
import com.jkky98.spubg.service.business.exception.MatchSyncValidationException;
import com.jkky98.spubg.service.implement.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchSyncService {

    private final MatchReader matchReader;
    private final PubgApiRequestService pubgApiRequestService;
    private final MemberMatchSyncService memberMatchSyncService;

    /**
     * 매치 데이터를 통해 멤버-매치 데이터 추가 및 누락 매치 데이터 보완
     * 매치 엔티티는 이미 DB에 존재하는 match를 받게 된다.
     * @param match
     */
    @Transactional
    public void sync(Match match) {
        Match matchRead = matchReader.read(match.getId());

        try {

            JsonNode rootNode = getJsonNodeFromPubgMatchAPI(matchRead);

            validMatchSync(rootNode);

            /**
             * 1. 등록된 멤버에 대해 매치 데이터 안에 멤버들 존재하는지 검사
             * 2. 멤버-매치 엔티티에 누락 멤버-매치 엔티티 존재하는지 검사
             * 3. 누락 존재할 경우 멤버-매치에 추가
             */
            memberMatchSyncService.syncMemberMatchIfMissing(rootNode, matchRead);

            syncMatch(rootNode, matchRead);
        } catch (MatchSyncValidationException e) {
            log.warn(e.getMessage());
            garbageMatchSync(matchRead);
        }

    }

    private static void syncMatch(JsonNode rootNode, Match matchRead) {
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
                createdAt
        );
    }

    private static void updateEntity(Match matchRead, String mapName, String assetId, String assetURL, LocalDateTime createdAt) {
        matchRead.setBoolIsAnalysis(true);
        matchRead.setMap(mapName);
        matchRead.setAssetId(assetId);
        matchRead.setAssetUrl(assetURL);
        matchRead.setCreatedAt(createdAt);
        matchRead.setGameMode(GameMode.SQUAD);
    }

    private static String getAssetId(JsonNode rootNode) {
        JsonNode assetsArrayNode = rootNode.path("data").path("relationships").path("assets").path("data");

        if (assetsArrayNode.isArray() && !assetsArrayNode.isEmpty()) {
            for (JsonNode assetNode : assetsArrayNode) {
                if (assetNode.path("type").asText().equals("asset")) {
                    String assetId = assetNode.path("id").asText();

                    if (assetId.isEmpty()) {
                        fail("type : assetId가 비어있습니다. 추출할 수 없습니다.");
                    }

                    return assetId;
                }
            }
        }

        throw new MatchSyncValidationException("assetId를 찾을 수 없습니다.");
    }

    private static String getAssetURL(JsonNode rootNode, String assetId) {
        for (JsonNode includedNode : rootNode.path("included")) {
            if (includedNode.path("type").asText().equals("asset")
                && includedNode.path("id").asText().equals(assetId)) {
                String returnUrl = includedNode.path("attributes").path("URL").asText();

                if (returnUrl.isEmpty()) {
                    fail("assetUrl이 존재하지 않습니다.");
                }

                return returnUrl;
            }
        }
        throw new MatchSyncValidationException("assetUrl이 존재하지 않습니다.");
    }

    private static LocalDateTime getCreatedAt(JsonNode attNode) {
        String createdAtString = attNode.path("createdAt").asText();
        
        if (createdAtString.isEmpty()) {
            fail("created이 attNode에 존재하지 않습니다.");
        }

        return Instant.parse(createdAtString)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime();
    }

    private static String getMapNameFromAttNode(JsonNode attNode) {
        String mapNameRaw = attNode.path("mapName").asText();
        if (mapNameRaw.isEmpty()) {
            fail("mapName이 attNode에 존재하지 않습니다.");
        }
        return mapNameRaw;
    }

    private void validMatchSync(JsonNode rootNode) {
        JsonNode dataNode = rootNode.path("data");

        if (dataNode.isMissingNode()) {
            String errorDetail = Optional.of(dataNode)
                    .map(n -> n.path("error"))
                    .filter(n -> !n.isMissingNode())
                    .map(n -> n.path("detail").asText())
                    .orElse(null);

            fail(errorDetail != null
                    ? "매치 json 데이터를 읽을 수 없습니다. error Detail : " + errorDetail
                    : "매치 json 데이터를 읽을 수 없습니다.");
        }

        if (!"match".equals(dataNode.path("type").asText())) {
            fail("서버에서 정의한 올바른 매치 타입이 아닙니다.");
        }

        JsonNode attNode = dataNode.path("attributes");

        if (attNode.isMissingNode()) {
            fail("data노드는 존재하지만, attributes 노드가 존재하지 않습니다.");
        }

        if (attNode.path("isCustomMatch").asBoolean()) {
            fail("이 매치는 커스텀 매치이므로 분석 대상이 아닙니다.");
        }

        if (!"official".equals(attNode.path("matchType").asText())) {
            fail("오피셜 타입의 매치가 아니므로 분석 대상이 아닙니다.");
        }

        if (!"squad".equals(attNode.path("gameMode").asText())) {
            fail("게임 모드가 스쿼드가 아니므로 분석 대상이 아닙니다.");
        }

        if (rootNode.path("included").isMissingNode()) {
            fail("included 노드가 존재하지 않습니다.");
        }
    }

    private static void fail(String message) {
        // 실패시 예외 던짐
        throw new MatchSyncValidationException(message);
    }

    private static void garbageMatchSync(Match matchRead) {
        // 매치 엔티티를 분석 과정이 없었으나 분석 완료로 처리해버림.
        // 이유 : 위 예외를 거친 매치 엔티티는 분석필요 없음.
        // 분석 완료된 매치는 분석 대상에 포함되지 않음.
        matchRead.setBoolIsAnalysis(true);
        matchRead.setGameMode(GameMode.OTHER);
    }

    private JsonNode getJsonNodeFromPubgMatchAPI(Match match) {
        return pubgApiRequestService.requestMatch(match.getMatchApiId());
    }
}
