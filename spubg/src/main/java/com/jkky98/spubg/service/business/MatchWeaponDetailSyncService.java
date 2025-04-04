package com.jkky98.spubg.service.business;

import com.fasterxml.jackson.databind.JsonNode;
import com.jkky98.spubg.domain.*;
import com.jkky98.spubg.service.implement.MatchWeaponDetailWriter;
import com.jkky98.spubg.service.implement.MemberMatchReader;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jkky98.spubg.pubg.enums.TelemetryEventType.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchWeaponDetailSyncService {

    private final MemberMatchReader memberMatchReader;
    private final MatchWeaponDetailWriter matchWeaponDetailWriter;

    @Transactional
    public void sync(Long memberMatchId, JsonNode rootNode) {
        MemberMatch memberMatch = memberMatchReader.read(memberMatchId);

        if (!rootNode.isArray() || rootNode.isEmpty()) {
            throw new RuntimeException("json root 노드가 의도된 형식이 아닙니다.");
        }

        Map<EventType, List<JsonNode>> filteredEvents = filterEvents(rootNode, memberMatch.getMember().getAccountId());

        List<JsonNode> attackNodes = filteredEvents.get(EventType.ATTACK);
        List<JsonNode> damageNodes = filteredEvents.get(EventType.DAMAGE);
        List<JsonNode> groggyNodes = filteredEvents.get(EventType.GROGGY);

        Map<String, WeaponHistory> weaponHistoryMap = buildWeaponHistoryMap(attackNodes, damageNodes, groggyNodes);

        List<MatchWeaponDetail> matchWeaponDetails = new ArrayList<>();

        weaponHistoryMap.forEach((attackId, weaponHistory) -> {
            MatchWeaponDetail mwDetail = ofMatchWeaponDetail(attackId, weaponHistory, memberMatch);
            matchWeaponDetails.add(mwDetail);
        });

        memberMatch.setBoolIsAnalysis(true);

        List<MatchWeaponDetail> matchWeaponDetailsSaved = matchWeaponDetailWriter.saveAll(matchWeaponDetails);

        log.debug("MatchWeaponDetail 데이터 등록 성공 - 개수 : {}, 유저 - {}, 매치 - {}",
                matchWeaponDetailsSaved.size(),
                memberMatch.getMember().getUsername(),
                memberMatch.getMatch().getMatchApiId()
                );

    }

    private static MatchWeaponDetail ofMatchWeaponDetail(String attackId, WeaponHistory weaponHistory, MemberMatch memberMatch) {
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
        return mwDetail;
    }

    private enum EventType {
        ATTACK, DAMAGE, GROGGY
    }

    private Map<EventType, List<JsonNode>> filterEvents(JsonNode rootNode, String accountId) {
        List<JsonNode> attackEvents = new ArrayList<>();
        List<JsonNode> damageEvents = new ArrayList<>();
        List<JsonNode> groggyEvents = new ArrayList<>();

        for (JsonNode eventNode : rootNode) {
            // _T 필드가 있어야 함
            if (!eventNode.has("_T")) {
                continue;
            }
            String eventType = eventNode.get("_T").asText();

            // attacker가 있고 accountId가 일치하는지 체크
            if (eventNode.has("attacker")
                    && eventNode.get("attacker").has("accountId")
                    && eventNode.get("attacker").get("accountId").asText().equals(accountId)) {

                // 각 이벤트 유형에 따라 분류
                if (LOG_PLAYER_ATTACK.getEventName().equals(eventType)) {
                    attackEvents.add(eventNode);
                } else if (LOG_PLAYER_TAKE_DAMAGE.getEventName().equals(eventType)) {
                    // 유효한 attackId 체크
                    if (eventNode.has("attackId") && !"-1".equals(eventNode.get("attackId").asText())) {
                        damageEvents.add(eventNode);
                    }
                } else if (LOG_PLAYER_MAKE_GROGGY.getEventName().equals(eventType)) {
                    // 유효한 attackId 체크
                    if (eventNode.has("attackId") && !"-1".equals(eventNode.get("attackId").asText())) {
                        groggyEvents.add(eventNode);
                    }
                }
            }
        }

        Map<EventType, List<JsonNode>> result = new EnumMap<>(EventType.class);
        result.put(EventType.ATTACK, attackEvents);
        result.put(EventType.DAMAGE, damageEvents);
        result.put(EventType.GROGGY, groggyEvents);

        return result;
    }

    private Map<String, WeaponHistory> buildWeaponHistoryMap(
            List<JsonNode> attackNodes,
            List<JsonNode> damageNodes,
            List<JsonNode> groggyNodes) {

        // groggyNodes를 미리 attackId를 키로 하는 Map으로 변환
        Map<String, JsonNode> groggyMap = groggyNodes.stream()
                .collect(Collectors.toMap(
                        node -> node.get("attackId").asText(),
                        node -> node,
                        (n1, n2) -> n1)); // 중복 키가 발생하면 첫 번째 값을 사용

        // attackNodes를 순회하며 초기 WeaponHistory Map 생성
        Map<String, WeaponHistory> weaponHistoryMap = attackNodes.stream()
                .collect(Collectors.toMap(
                        node -> node.get("attackId").asText(),
                        node -> {
                            String itemId = node.get("weapon").get("itemId").asText();
                            LocalDateTime createdAt = Instant.parse(node.get("_D").asText())
                                    .atZone(ZoneId.of("Asia/Seoul"))
                                    .toLocalDateTime();
                            BigDecimal attackerHealth = BigDecimal.valueOf(node.get("attacker").get("health").asDouble());
                            boolean attackerIsInVehicle = node.get("attacker").get("isInVehicle").asBoolean();
                            BigDecimal phase = BigDecimal.valueOf(node.get("common").get("isGame").asDouble());
                            BigDecimal attX = BigDecimal.valueOf(node.get("attacker").get("location").get("x").asDouble());
                            BigDecimal attY = BigDecimal.valueOf(node.get("attacker").get("location").get("y").asDouble());
                            BigDecimal attZ = BigDecimal.valueOf(node.get("attacker").get("location").get("z").asDouble());

                            return new WeaponHistory(
                                    WeaponName.fromKey(itemId),
                                    createdAt,
                                    attackerHealth,
                                    attackerIsInVehicle,
                                    phase,
                                    attX,
                                    attY,
                                    attZ);
                        }));

        // damageNodes를 순회하면서 WeaponHistory 업데이트
        damageNodes.forEach(damageNode -> {
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

                    // 맞은 위치 정보 읽기
                    BigDecimal damX = BigDecimal.valueOf(damageNode.get("victim").get("location").get("x").asDouble());
                    BigDecimal damY = BigDecimal.valueOf(damageNode.get("victim").get("location").get("y").asDouble());
                    BigDecimal damZ = BigDecimal.valueOf(damageNode.get("victim").get("location").get("z").asDouble());
                    // 거리 계산 (calculateDistance는 기존 메서드)
                    BigDecimal distance = calculateDistance(
                            weaponHistory.getAttX(),
                            weaponHistory.getAttY(),
                            weaponHistory.getAttZ(),
                            damX,
                            damY,
                            damZ);
                    weaponHistory.setDamDistance(distance);

                    // 해당 attackId에 해당하는 groggy 이벤트가 있는 경우 기절 여부 설정
                    if (groggyMap.containsKey(attackId)) {
                        weaponHistory.setGroggy(true);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });

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
