package com.jkky98.spubg.pubg.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.jkky98.spubg.pubg.util.TokenBucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class  PubgApiManager {
    private final WebClient webClient;
    private final PubgUtil pubgUtil;
    private final TokenBucket tokenBucket;

    private void consumeToken() {
        synchronized (tokenBucket) {
            while (!tokenBucket.tryConsume()) {
                try {
                    tokenBucket.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread was interrupted while waiting for token", e);
                }
            }
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

        log.info("Sending request to URL: {}", url);
        log.info("Authorization: {}", apiKey);
        log.info("Accept: {}", acceptHeader);

        return webClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, apiKey)
                .header(HttpHeaders.ACCEPT, acceptHeader)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnNext(response -> log.info("Response received: {}", response))  // 응답 로그
                .doOnError(error -> log.error("Error occurred: ", error))  // 오류 로그
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
}
