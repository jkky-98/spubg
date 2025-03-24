package com.jkky98.spubg.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.jkky98.spubg.domain.*;
import com.jkky98.spubg.pubg.request.PubgApiManager;
import com.jkky98.spubg.pubg.request.TelemetryEventType;
import com.jkky98.spubg.pubg.request.TelemetryRequestBuilder;
import com.jkky98.spubg.repository.MatchWeaponDetailRepository;
import com.jkky98.spubg.repository.MemberMatchRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.StreamSupport;

import static com.jkky98.spubg.pubg.request.TelemetryEventType.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberMatchService {

    private final MemberMatchRepository memberMatchRepository;
    private final MatchWeaponDetailRepository mwDetailRepository;
    private final TelemetryRequestBuilder telemetryRequestBuilder;

    @Transactional
    public void saveMatchWeaponDetail(MemberMatch memberMatch) throws JsonProcessingException {
        log.info("[텔레메트리 패치 작업] 📌 [START] Processing match weapon details for MemberMatch ID: {}", memberMatch.getId());

        MemberMatch memberMatchFind = memberMatchRepository.findById(memberMatch.getId()).orElseThrow();

        String telemetryUrl = memberMatchFind.getMatch().getAssetUrl();
        String accountId = memberMatchFind.getMember().getAccountId();

//        JsonNode rootNode = pubgApiManager.requestTelemetry(telemetryUrl);
        JsonNode rootNode = telemetryRequestBuilder
                .uri(telemetryUrl)
                .event(LOG_PLAYER_ATTACK)
                .event(LOG_PLAYER_TAKE_DAMAGE)
                .event(LOG_PLAYER_MAKE_GROGGY)
                .execute();

        log.info("[텔레메트리 패치 작업] ✅ Telemetry data successfully retrieved.");

        if (rootNode.isArray() && rootNode.size() > 0) {
            JsonNode firstNode = rootNode.get(0);
            log.info("[텔레메트리 패치 작업] 🔍 First telemetry event: {}", firstNode.toPrettyString()); // JSON을 보기 좋게 출력
        } else {
            log.warn("[텔레메트리 패치 작업] ⚠ No telemetry events found in the response.");
        }

        List<JsonNode> attackNodes = getLogPlayerAttackEvents(rootNode, accountId);
        List<JsonNode> damageNodes = getLogPlayerTakeDamage(rootNode, accountId);
        List<JsonNode> groggyNodes = getLogPlayerMakeGroggy(rootNode, accountId);

        log.info("[텔레메트리 패치 작업] 📊 Found {} LogPlayerAttack events for AccountID: {}", attackNodes.size(), accountId);
        log.info("[텔레메트리 패치 작업] 📊 Found {} LogPlayerTakeDamage events for AccountID: {}", damageNodes.size(), accountId);

        // key : attackId
        // value : WeaponHistory
        Map<String, WeaponHistory> weaponHistoryMap = buildWeaponHistoryMap(attackNodes, damageNodes, groggyNodes);

        log.info("[텔레메트리 패치 작업] 🔄 Built WeaponHistoryMap with {} entries", weaponHistoryMap.size());

        List<MatchWeaponDetail> matchWeaponDetails = new ArrayList<>();

        weaponHistoryMap.forEach((attackId, weaponHistory) -> {

            MatchWeaponDetail mwDetail = MatchWeaponDetail.builder()
                    .attackId(attackId)
                    .damageWhere(weaponHistory.damageWhere)
                    .weaponType(weaponHistory.weaponType)
                    .weaponName(weaponHistory.weaponName)
                    .damage(weaponHistory.damage)
                    .createdAt(weaponHistory.createdAt)
                    .attackerHealth(weaponHistory.attackerHealth)
                    .attackerIsInVehicle(weaponHistory.attackerIsinVehicle)
                    .phase(weaponHistory.phase)
                    .memberMatch(memberMatch)
                    .damDistance(weaponHistory.damDistance)
                    .groggy(weaponHistory.groggy)
                    .build();

            matchWeaponDetails.add(mwDetail);
        });

        MemberMatch memberMatchUpdated = memberMatchRepository.findById(memberMatchFind.getId())
                .orElseThrow(() -> new RuntimeException("MemberMatch not found with ID: " + memberMatch.getId()));

        memberMatchUpdated.setBoolIsAnalysis(true);
        log.info("[텔레메트리 패치 작업] 🔄 Updated MemberMatch ID: {} -> boolIsAnalysis = true", memberMatchFind.getId());

        log.info("[텔레메트리 패치 작업] 💾 Saving {} match weapon details to the database.", matchWeaponDetails.size());
        mwDetailRepository.saveAll(matchWeaponDetails);
        log.info("[텔레메트리 패치 작업] ✅ Match weapon details saved successfully.");

        log.info("[텔레메트리 패치 작업] 📌 [END] Processing completed for MemberMatch ID: {}", memberMatchFind.getId());
    }


    @Transactional
    public List<MemberMatch> getMemberMatchNeedToAnaysis() {
        return memberMatchRepository.findByMatchIsAnalyzedAndSquad();
    }

    private List<JsonNode> getLogPlayerAttackEvents(JsonNode rootNode, String accountId) {
        log.info("[텔레메트리 패치 작업] 🔎 Filtering LogPlayerAttack events for accountId: {}", accountId);

        List<JsonNode> attackEvents = StreamSupport.stream(rootNode.spliterator(), false)
                .filter(eventNode -> {
                    boolean hasT = eventNode.has("_T");
                    boolean isLogPlayerAttack = hasT && LOG_PLAYER_ATTACK.getEventName().equals(eventNode.get("_T").asText());
                    boolean hasAttacker = eventNode.has("attacker") && eventNode.get("attacker").has("accountId");
                    boolean matchesAccountId = hasAttacker && eventNode.get("attacker").get("accountId").asText().equals(accountId);

                    log.debug("[텔레메트리 패치 작업] 🧐 Event Type: {}, Has Attacker: {}, Matches AccountId: {}",
                            hasT ? eventNode.get("_T").asText() : "N/A",
                            hasAttacker,
                            matchesAccountId);

                    return isLogPlayerAttack && matchesAccountId;
                })
                .toList();

        log.info("[텔레메트리 패치 작업] ✅ Found {} LogPlayerAttack events for accountId: {}", attackEvents.size(), accountId);
        return attackEvents;
    }


    private List<JsonNode> getLogPlayerTakeDamage(JsonNode rootNode, String attackerAccountId) {
        log.info("[텔레메트리 패치 작업] 🔎 Filtering LogPlayerTakeDamage events for attackerAccountId: {}", attackerAccountId);

        List<JsonNode> damageEvents = StreamSupport.stream(rootNode.spliterator(), false)
                .filter(eventNode -> {
                    boolean hasT = eventNode.has("_T");
                    boolean isLogPlayerTakeDamage = hasT && LOG_PLAYER_TAKE_DAMAGE.getEventName().equals(eventNode.get("_T").asText());
                    boolean hasAttacker = eventNode.has("attacker") && eventNode.get("attacker").has("accountId");
                    boolean matchesAttackerAccountId = hasAttacker && eventNode.get("attacker").get("accountId").asText().equals(attackerAccountId);
                    boolean hasAttackId = eventNode.has("attackId");
                    boolean validAttackId = hasAttackId && !"-1".equals(eventNode.get("attackId").asText());

                    log.debug("[텔레메트리 패치 작업] 🧐 Event Type: {}, Has Attacker: {}, Matches Attacker AccountId: {}, Has attackId: {}, Valid AttackId: {}",
                            hasT ? eventNode.get("_T").asText() : "N/A",
                            hasAttacker,
                            matchesAttackerAccountId,
                            hasAttackId,
                            validAttackId);

                    return isLogPlayerTakeDamage && matchesAttackerAccountId && validAttackId;
                })
                .toList();

        log.info("[텔레메트리 패치 작업] ✅ Found {} LogPlayerTakeDamage events for attackerAccountId: {}", damageEvents.size(), attackerAccountId);
        return damageEvents;
    }

    private List<JsonNode> getLogPlayerMakeGroggy(JsonNode rootNode, String attackerAccountId) {
        log.info("[텔레메트리 패치 작업] 🔎 Filtering LogPlayerMakeGroggy events for attackerAccountId: {}", attackerAccountId);

        List<JsonNode> groggyEvents = StreamSupport.stream(rootNode.spliterator(), false)
                .filter(eventNode -> {
                    boolean hasT = eventNode.has("_T");
                    boolean isLogPlayerMakeGroggy = hasT && LOG_PLAYER_MAKE_GROGGY.getEventName().equals(eventNode.get("_T").asText());
                    boolean hasAttacker = eventNode.has("attacker") && eventNode.get("attacker").has("accountId");
                    boolean matchesAttackerAccountId = hasAttacker && eventNode.get("attacker").get("accountId").asText().equals(attackerAccountId);
                    boolean hasAttackId = eventNode.has("attackId");
                    boolean validAttackId = hasAttackId && !"-1".equals(eventNode.get("attackId").asText());

                    log.debug("[텔레메트리 패치 작업] 🧐 Event Type: {}, Has Attacker: {}, Matches Attacker AccountId: {}, Has attackId: {}, Valid AttackId: {}",
                            hasT ? eventNode.get("_T").asText() : "N/A",
                            hasAttacker,
                            matchesAttackerAccountId,
                            hasAttackId,
                            validAttackId);
                    return isLogPlayerMakeGroggy && matchesAttackerAccountId && validAttackId;
                })
                .toList();

        log.info("[텔레메트리 패치 작업] ✅ Found {} LogPlayerMakeGroggy events for attackerAccountId: {}", groggyEvents.size(), attackerAccountId);
        return groggyEvents;
    }


    private Map<String, WeaponHistory> buildWeaponHistoryMap(List<JsonNode> attackNodes, List<JsonNode> damageNodes, List<JsonNode> groggyNodes) {
        Map<String, WeaponHistory> weaponHistoryMap = new HashMap<>();

        for (JsonNode attackNode : attackNodes) {
            String attackId = attackNode.get("attackId").asText();
            String itemId = attackNode.get("weapon").get("itemId").asText();
            LocalDateTime createdAt = Instant.parse(attackNode.get("_D").asText())
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toLocalDateTime();
            BigDecimal attackerHealth = BigDecimal.valueOf(attackNode.get("attacker").get("health").asDouble());
            boolean attackerIsInVehicle = attackNode.get("attacker").get("isInVehicle").asBoolean();
            BigDecimal phase = BigDecimal.valueOf(attackNode.get("common").get("isGame").asDouble());
            BigDecimal attX = BigDecimal.valueOf(attackNode.get("attacker").get("location").get("x").asDouble());
            BigDecimal attY = BigDecimal.valueOf(attackNode.get("attacker").get("location").get("y").asDouble());
            BigDecimal attZ = BigDecimal.valueOf(attackNode.get("attacker").get("location").get("z").asDouble());

            WeaponHistory weaponHistory = new WeaponHistory(WeaponName.fromKey(itemId), createdAt, attackerHealth, attackerIsInVehicle, phase, attX, attY, attZ);

            weaponHistoryMap.put(attackId, weaponHistory);
        }

        for (JsonNode damageNode : damageNodes) {
            try {
                String attackId = damageNode.get("attackId").asText();
                WeaponHistory weaponHistory = weaponHistoryMap.get(attackId);

                if (weaponHistory != null) {
                    // 데미지 기록 설정
                    weaponHistory.setDamage(BigDecimal.valueOf(damageNode.get("damage").asDouble()));
                    // 무기 이름 설정
                    weaponHistory.setWeaponName(WeaponName.fromKey(damageNode.get("damageCauserName").asText()));
                    // 맞은 부위 설정
                    weaponHistory.setDamageWhere(DamageWhere.fromkey(damageNode.get("damageReason").asText()));
                    // 무기 카테고리 설정
                    weaponHistory.setWeaponType(WeaponType.getWeaponType(weaponHistory.getWeaponName()));
                    // 맞은 x,y,z 위치
                    BigDecimal damX = BigDecimal.valueOf(damageNode.get("victim").get("location").get("x").asDouble());
                    BigDecimal damY = BigDecimal.valueOf(damageNode.get("victim").get("location").get("y").asDouble());
                    BigDecimal damZ = BigDecimal.valueOf(damageNode.get("victim").get("location").get("z").asDouble());
                    // 거리 설정
                    BigDecimal distance = calculateDistance(weaponHistory.attX, weaponHistory.attY, weaponHistory.attZ, damX, damY, damZ);
                    weaponHistory.setDamDistance(distance);
                    // 기절 여부 설정
                    Optional<JsonNode> matchingGroggyNode = groggyNodes.stream()
                            .filter(groggyNode -> groggyNode.get("attackId").asText().equals(attackId))
                            .findFirst();
                    matchingGroggyNode.ifPresent(node -> {
                            weaponHistory.setGroggy(true);
                    });
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return weaponHistoryMap;
    }

    @Data
    private static class WeaponHistory {

        private WeaponName weaponName;
        private BigDecimal damage = BigDecimal.ZERO;
        private WeaponType weaponType;
        private DamageWhere damageWhere;
        private LocalDateTime createdAt;

        // 3.18 추가 필드
        private BigDecimal attackerHealth;

        private boolean attackerIsinVehicle;

        // isGame
        private BigDecimal phase;

        private BigDecimal attX;

        private BigDecimal attY;

        private BigDecimal attZ;

        private BigDecimal damDistance;

        private boolean groggy = false;

        public WeaponHistory(WeaponName weaponName,
                             LocalDateTime createdAt,
                             BigDecimal attackerHealth,
                             boolean attackerIsinVehicle,
                             BigDecimal phase,
                             BigDecimal attX,
                             BigDecimal attY,
                             BigDecimal attZ) {
            this.weaponName = weaponName;
            this.createdAt = createdAt;
            this.attackerHealth = attackerHealth;
            this.attackerIsinVehicle = attackerIsinVehicle;
            this.phase = phase;
            this.attX = attX;
            this.attY = attY;
            this.attZ = attZ;
        }
    }

    public static BigDecimal calculateDistance(BigDecimal x1, BigDecimal y1, BigDecimal z1,
                                               BigDecimal x2, BigDecimal y2, BigDecimal z2) {
        // (x2 - x1)^2
        BigDecimal deltaX = x2.subtract(x1).pow(2);
        // (y2 - y1)^2
        BigDecimal deltaY = y2.subtract(y1).pow(2);
        // (z2 - z1)^2
        BigDecimal deltaZ = z2.subtract(z1).pow(2);

        // sqrt((x2 - x1)^2 + (y2 - y1)^2 + (z2 - z1)^2)
        BigDecimal sum = deltaX.add(deltaY).add(deltaZ);

        // Math.sqrt()를 사용하여 BigDecimal 값의 제곱근을 구함
        BigDecimal distance = BigDecimal.valueOf(Math.sqrt(sum.doubleValue()));

        // 소수점 2자리까지 반올림하여 반환
        return distance.setScale(2, RoundingMode.HALF_UP);
    }
}
