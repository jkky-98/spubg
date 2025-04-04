package com.jkky98.spubg.pubg.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.jkky98.spubg.pubg.ratelimit.TokenManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


@Service
@RequiredArgsConstructor
@Slf4j
public class PubgApiManager {
    private final WebClient webClient;
    private final PubgUtil pubgUtil;
    private final TokenManager tokenManager;

    /**
     *  WebClient 요청을 처리하는 공통 메서드
     * @param endpoint API 요청할 URI 경로 (예: "/seasons")
     * @return JsonNode (응답 데이터)
     */
    public JsonNode get(String endpoint) {
        tokenManager.consume();

        return webClient.get()
                .uri(pubgUtil.getBaseUrl() + endpoint)
                .header(HttpHeaders.AUTHORIZATION, pubgUtil.getApiKey())
                .header(HttpHeaders.ACCEPT, pubgUtil.getAccept())
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnNext(r -> log.info("[PubgApiClient][get] webClient 외부 API 요청 성공"))
                .doOnError(e -> log.error("[PubgApiClient][get] webClient 외부 API 요청 실패", e))
                .block();
    }
}
