package com.jkky98.spubg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jkky98.spubg.domain.GameMode;
import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.Member;
import com.jkky98.spubg.domain.MemberMatch;
import com.jkky98.spubg.pubg.request.PubgApiRequestService;
import com.jkky98.spubg.repository.MatchRepository;
import com.jkky98.spubg.repository.MemberMatchRepository;
import com.jkky98.spubg.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final PubgApiRequestService pubgApiRequestService;
    private final MemberRepository memberRepository;
    private final MemberMatchRepository memberMatchRepository;

    @Transactional
    public List<Match> saveAll(List<Match> matchList) {
        return matchRepository.saveAll(matchList);
    }

    @Transactional
    public Match save(Match match) {
        return matchRepository.save(match);
    }

    /**
     * 매치 데이터 처리
     */
    @Transactional
    public void processMatch(Match match) {
        try {
            log.info("[매치 패치 작업] 🔄 [START] Processing Match: {}", match.getMatchApiId());

            JsonNode rootNode = pubgApiRequestService.requestMatch(match.getMatchApiId());
            JsonNode dataNode = rootNode.get("data");

            log.debug("[매치 패치 작업] 📥 Match data fetched for Match ID: {} | Data: {}", match.getMatchApiId(), dataNode);

            if (!isProcess(dataNode, match)) {
                log.warn("[매치 패치 작업] ⚠ Match {} skipped due to isProcess check.", match.getMatchApiId());
                return;
            }

            JsonNode attNode = dataNode.get("attributes");

            log.debug("[매치 패치 작업] 🛠 Match attributes retrieved for Match ID: {}", match.getMatchApiId());

            if (!checkGameMode(attNode, match)) {
                log.warn("[매치 패치 작업] ⚠ Match {} skipped due to unsupported game mode.", match.getMatchApiId());
                return;
            }

            log.info("[매치 패치 작업] 📝 Updating match data for Match ID: {}", match.getMatchApiId());
            updateMatch(rootNode, match);

            log.info("[매치 패치 작업] ✅ [SUCCESS] Match {} processed successfully", match.getMatchApiId());
        } catch (Exception e) {
            log.error("[매치 패치 작업] ❌ [ERROR] Error processing Match {} | Exception: {}", match.getMatchApiId(), e.getMessage(), e);
        }
    }

    private boolean checkGameMode(JsonNode attNode, Match match) {
        if (!attNode.get("gameMode").asText().equals("squad")) {
            Match matchUpdated = matchRepository.findById(match.getId()).orElseThrow(EntityNotFoundException::new);
            matchUpdated.setGameMode(GameMode.OTHER);
            matchUpdated.setBoolIsAnalysis(true);
            return false;
        }
        return true;
    }

    private boolean isProcess(JsonNode dataNode, Match match) {
        if (!dataNode.get("type").asText().equals("match")) {
            garbageMatch(match);
            return false;
        }

        JsonNode attNode = dataNode.get("attributes");

        if (attNode.get("isCustomMatch").asBoolean()) {
            garbageMatch(match);
            return false;
        }

        if (!attNode.get("matchType").asText().equals("official")) {
            garbageMatch(match);
            return false;
        }

        return true;
    }

    private void garbageMatch(Match match) {
        Match matchUpdated = matchRepository.findById(match.getId()).orElseThrow(EntityNotFoundException::new);
        matchUpdated.setBoolIsAnalysis(true);
        matchUpdated.setGameMode(GameMode.OTHER);
    }

    private void updateMatch(JsonNode rootNode, Match match) {
        log.info("[매치 패치 작업] 🔹 업데이트 시작: Match ID = {}", match.getMatchApiId());
        Match matchUpdated = matchRepository.findById(match.getId()).orElseThrow(EntityNotFoundException::new);

        /**
         * 누락 플레이어 있다면 업데이트
         */
        List<String> searchCond = memberRepository.findAll()
                .stream()
                .map(Member::getAccountId)
                .toList();

        for (JsonNode includedNode : rootNode.get("included")) {
            if (includedNode.get("type").asText().equals("participant")) {
                String accountId = includedNode.get("attributes").get("stats").get("playerId").asText();
                if (searchCond.contains(accountId)) {
                    Optional<MemberMatch> byMemberAccountIdAndMatch =
                            memberMatchRepository.findByMemberAccountIdAndMatch(accountId, matchUpdated);

                    byMemberAccountIdAndMatch.orElseGet(
                            () -> {
                                Member member = memberRepository.findByAccountId(accountId).orElseThrow();
                                MemberMatch memberMatch = MemberMatch.builder()
                                        .member(member)
                                        .match(matchUpdated)
                                        .boolIsAnalysis(false)
                                        .build();

                                memberMatchRepository.save(memberMatch);
                                return memberMatch;
                            });
                }
            }
        }


        matchUpdated.setBoolIsAnalysis(true);
        log.info("[매치 패치 작업] ✅ 분석 여부 설정: boolIsAnalysis = {}", matchUpdated.isBoolIsAnalysis());

        String mapName = rootNode.get("data").get("attributes").get("mapName").asText();
        LocalDateTime createdAt = parseCreatedAt(rootNode);
        String displayMapName = GameMap.getDisplayName(mapName);
        matchUpdated.setMap(displayMapName);
        matchUpdated.setCreatedAt(createdAt);
        log.info("[매치 패치 작업] ✅ 맵 정보 업데이트: mapName = {} -> displayName = {}", mapName, displayMapName);

        matchUpdated.setGameMode(GameMode.SQUAD);
        log.info("[매치 패치 작업] ✅ 게임 모드 설정: gameMode = {}", matchUpdated.getGameMode());

        JsonNode assetsNode = rootNode.get("data").get("relationships").get("assets").get("data");
        if (assetsNode.isArray() && assetsNode.size() > 0) {
            String assetId = assetsNode.get(0).get("id").asText();
            matchUpdated.setAssetId(assetId);
            log.info("[매치 패치 작업] ✅ Asset ID 설정: assetId = {}", assetId);

            for (JsonNode includedNode : rootNode.get("included")) {
                if (includedNode.get("id").asText().equals(assetId)) {
                    String assetUrl = includedNode.get("attributes").get("URL").asText();
                    matchUpdated.setAssetUrl(assetUrl);
                    log.info("[매치 패치 작업] ✅ Asset URL 설정: assetUrl = {}", assetUrl);
                    break;
                }
            }
        } else {
            log.warn("[매치 패치 작업] ⚠️ Asset ID가 존재하지 않음. Match ID = {}", matchUpdated.getMatchApiId());
        }

        log.info("[매치 패치 작업] ✅ 업데이트 완료: Match ID = {}", matchUpdated.getMatchApiId());
    }

    private static LocalDateTime parseCreatedAt(JsonNode rootNode) {
        String createdAtString = rootNode.get("data").get("attributes").get("createdAt").asText();

        return Instant.parse(createdAtString)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime();
    }
}
