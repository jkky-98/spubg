package com.jkky98.spubg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jkky98.spubg.domain.*;
import com.jkky98.spubg.pubg.request.PubgApiManager;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberMatchService {

    private final MemberMatchRepository memberMatchRepository;
    private final MatchWeaponDetailRepository mwDetailRepository;
    private final PubgApiManager pubgApiManager;

    @Transactional
    public void saveMatchWeaponDetail(MemberMatch memberMatch) {
        log.info("📌 [START] Processing match weapon details for MemberMatch ID: {}", memberMatch.getId());

        MemberMatch memberMatchFind = memberMatchRepository.findById(memberMatch.getId()).orElseThrow();

        String telemetryUrl = memberMatchFind.getMatch().getAssetUrl();
        String accountId = memberMatchFind.getMember().getAccountId();

        JsonNode rootNode = pubgApiManager.requestTelemetry(telemetryUrl);

        log.info("✅ Telemetry data successfully retrieved.");

        if (rootNode.isArray() && rootNode.size() > 0) {
            JsonNode firstNode = rootNode.get(0);
            log.info("🔍 First telemetry event: {}", firstNode.toPrettyString()); // JSON을 보기 좋게 출력
        } else {
            log.warn("⚠ No telemetry events found in the response.");
        }

        List<JsonNode> attackNodes = getLogPlayerAttackEvents(rootNode, accountId);
        List<JsonNode> damageNodes = getLogPlayerTakeDamage(rootNode, accountId);

        log.info("📊 Found {} LogPlayerAttack events for AccountID: {}", attackNodes.size(), accountId);
        log.info("📊 Found {} LogPlayerTakeDamage events for AccountID: {}", damageNodes.size(), accountId);

        // key : attackId
        // value : WeaponHistory
        Map<String, WeaponHistory> weaponHistoryMap = buildWeaponHistoryMap(attackNodes, damageNodes);

        log.info("🔄 Built WeaponHistoryMap with {} entries", weaponHistoryMap.size());

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
                    .damDistnace(weaponHistory.damDistance)
                    .build();

            matchWeaponDetails.add(mwDetail);
        });

        MemberMatch memberMatchUpdated = memberMatchRepository.findById(memberMatchFind.getId())
                .orElseThrow(() -> new RuntimeException("MemberMatch not found with ID: " + memberMatch.getId()));

        memberMatchUpdated.setBoolIsAnalysis(true);
        log.info("🔄 Updated MemberMatch ID: {} -> boolIsAnalysis = true", memberMatchFind.getId());

        log.info("💾 Saving {} match weapon details to the database.", matchWeaponDetails.size());
        mwDetailRepository.saveAll(matchWeaponDetails);
        log.info("✅ Match weapon details saved successfully.");

        log.info("📌 [END] Processing completed for MemberMatch ID: {}", memberMatchFind.getId());
    }


    @Transactional
    public List<MemberMatch> getMemberMatchNeedToAnaysis() {
        return memberMatchRepository.findByMatchIsAnalyzedAndSquad();
    }

    private List<JsonNode> getLogPlayerAttackEvents(JsonNode rootNode, String accountId) {
        log.info("🔎 Filtering LogPlayerAttack events for accountId: {}", accountId);

        List<JsonNode> attackEvents = StreamSupport.stream(rootNode.spliterator(), false)
                .filter(eventNode -> {
                    boolean hasT = eventNode.has("_T");
                    boolean isLogPlayerAttack = hasT && "LogPlayerAttack".equals(eventNode.get("_T").asText());
                    boolean hasAttacker = eventNode.has("attacker") && eventNode.get("attacker").has("accountId");
                    boolean matchesAccountId = hasAttacker && eventNode.get("attacker").get("accountId").asText().equals(accountId);

                    log.debug("🧐 Event Type: {}, Has Attacker: {}, Matches AccountId: {}",
                            hasT ? eventNode.get("_T").asText() : "N/A",
                            hasAttacker,
                            matchesAccountId);

                    return isLogPlayerAttack && matchesAccountId;
                })
                .toList();

        log.info("✅ Found {} LogPlayerAttack events for accountId: {}", attackEvents.size(), accountId);
        return attackEvents;
    }


    private List<JsonNode> getLogPlayerTakeDamage(JsonNode rootNode, String attackerAccountId) {
        log.info("🔎 Filtering LogPlayerTakeDamage events for attackerAccountId: {}", attackerAccountId);

        List<JsonNode> damageEvents = StreamSupport.stream(rootNode.spliterator(), false)
                .filter(eventNode -> {
                    boolean hasT = eventNode.has("_T");
                    boolean isLogPlayerTakeDamage = hasT && "LogPlayerTakeDamage".equals(eventNode.get("_T").asText());
                    boolean hasAttacker = eventNode.has("attacker") && eventNode.get("attacker").has("accountId");
                    boolean matchesAttackerAccountId = hasAttacker && eventNode.get("attacker").get("accountId").asText().equals(attackerAccountId);
                    boolean hasAttackId = eventNode.has("attackId");
                    boolean validAttackId = hasAttackId && !"-1".equals(eventNode.get("attackId").asText());

                    log.debug("🧐 Event Type: {}, Has Attacker: {}, Matches Attacker AccountId: {}, Has attackId: {}, Valid AttackId: {}",
                            hasT ? eventNode.get("_T").asText() : "N/A",
                            hasAttacker,
                            matchesAttackerAccountId,
                            hasAttackId,
                            validAttackId);

                    return isLogPlayerTakeDamage && matchesAttackerAccountId && validAttackId;
                })
                .toList();

        log.info("✅ Found {} LogPlayerTakeDamage events for attackerAccountId: {}", damageEvents.size(), attackerAccountId);
        return damageEvents;
    }


    private Map<String, WeaponHistory> buildWeaponHistoryMap(List<JsonNode> attackNodes, List<JsonNode> damageNodes) {
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

                    BigDecimal distance = calculateDistance(weaponHistory.attX, weaponHistory.attY, weaponHistory.attZ, damX, damY, damZ);
                    weaponHistory.setDamDistance(distance);
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
