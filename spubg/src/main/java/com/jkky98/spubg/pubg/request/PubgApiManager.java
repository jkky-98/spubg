package com.jkky98.spubg.pubg.request;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jkky98.spubg.pubg.util.TokenBucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class  PubgApiManager {
    private final WebClient webClient;
    private final PubgUtil pubgUtil;
    private final TokenBucket tokenBucket;
    private final ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());

    private void consumeToken() {
        synchronized (tokenBucket) {
            long startTime = System.currentTimeMillis();
            while (!tokenBucket.tryConsume()) {
                long waitedTime = System.currentTimeMillis() - startTime;
                if (waitedTime >= 60000 * 60 * 3) {
                    log.warn("[토큰 버킷] ⏳ Waited for 20 seconds, but still no token available! Retrying...");
                } else {
                    log.info("[토큰 버킷] 🚦 No tokens available. Waiting... (Elapsed: {} ms)", waitedTime);
                }

                try {
                    tokenBucket.wait(60000 * 60 * 3 - waitedTime); // 남은 대기 시간만큼만 대기
                    log.info("[토큰 버킷] 🔔 Woke up! Retrying token consumption...");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("[토큰 버킷] ❌ Thread was interrupted while waiting for token", e);
                    throw new RuntimeException("Thread was interrupted while waiting for token", e);
                }
            }

            log.info("[토큰 버킷] ✅ Token consumed successfully! Remaining tokens: {}", tokenBucket.getAvailableTokens());
        }
    }



    /**
     *  WebClient 요청을 처리하는 공통 메서드
     * @param endpoint API 요청할 URI 경로 (예: "/seasons")
     * @return JsonNode (응답 데이터)
     */
    private JsonNode sendRequest(String endpoint) {
        consumeToken();  // 토큰 소비 로직 실행

        // 로그 출력
        String url = pubgUtil.getBaseUrl() + endpoint;
        String apiKey = pubgUtil.getApiKey();
        String acceptHeader = pubgUtil.getAccept();

        return webClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, apiKey)
                .header(HttpHeaders.ACCEPT, acceptHeader)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnNext(response -> log.info("[PUBGAPI] Response received: {}", response))  // 응답 로그
                .doOnError(error -> log.error("[PUBGAPI] Error occurred: ", error))  // 오류 로그
                .block();
    }

    /**
     *  시즌 정보 요청
     */
    public JsonNode requestSeason() {
        return sendRequest("/seasons");
    }

    /**
     *  플레이어 정보 요청
     */
    public JsonNode requestMember(String username) {
        return sendRequest("/players?filter[PlayerNames]=" + username);
    }

    /**
     *  특정 매치 정보 요청
     */
    public JsonNode requestMatch(String matchId) {
        return sendRequest("/matches/" + matchId);
    }

    /**
     * 플레이어 여러 명 정보 요청 (최대 10명)
     */
    public JsonNode requestManyMember(List<String> usernames) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < usernames.size(); i++) {
            sb.append(usernames.get(i));
            if (i < usernames.size() - 1) { // 마지막 요소가 아닐 때만 "," 추가
                sb.append(",");
            }
        }

        return sendRequest("/players?filter[playerNames]=" + sb);
    }

    /**
     * telemtry 데이터 가져오기
     */
    public JsonNode requestTelemetry(String telemetryUrl) {
        log.info("📡 Fetching telemetry data from: {}", telemetryUrl);

        try {
            String jsonResponse = webClient.get()
                    .uri(telemetryUrl)
                    .header(HttpHeaders.ACCEPT, pubgUtil.getAccept())
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .retrieve()
                    .bodyToMono(String.class) // 🔥 String으로 직접 변환
                    .doOnNext(response -> log.info("[PUBGAPI] ✅ Response received (size={} bytes)", response.length()))
                    .doOnError(error -> log.error("[PUBGAPI] ❌ Error fetching telemetry data: ", error))
                    .block();

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                throw new RuntimeException("[PUBGAPI] ❌ Empty response from telemetry API");
            }

            JsonNode rootNode = objectMapper.readTree(jsonResponse); // JSON 변환
            log.info("[PUBGAPI] ✅ Successfully parsed telemetry JSON");

            // 필요한 이벤트만 필터링하여 JsonNode에 담아 반환
            ArrayNode filteredEvents = objectMapper.createArrayNode();
            rootNode.forEach(node -> {
                if (node.has("_T")) {
                    String eventType = node.get("_T").asText();
                    if ("LogPlayerAttack".equals(eventType) || "LogPlayerTakeDamage".equals(eventType)) {
                        filteredEvents.add(node);
                    }
                }
            });

            log.info("[PUBGAPI] 📊 Extracted {} relevant events", filteredEvents.size());
            return filteredEvents; // 최종적으로 필터링된 JsonNode 반환
        } catch (WebClientResponseException e) {
            log.error("[PUBGAPI] ❌ WebClientResponseException: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("[PUBGAPI] ❌ JSON parsing error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("[PUBGAPI] ❌ General Exception occurred: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
