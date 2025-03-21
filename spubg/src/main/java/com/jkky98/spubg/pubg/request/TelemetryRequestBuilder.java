package com.jkky98.spubg.pubg.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelemetryRequestBuilder {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final PubgUtil pubgUtil;

    private String telemetryUrl;
    private final Set<String> eventTypes = new HashSet<>();

    public TelemetryRequestBuilder uri(String telemetryUrl) {
        this.telemetryUrl = telemetryUrl;
        return this;
    }

    public TelemetryRequestBuilder event(TelemetryEventType eventType) {
        this.eventTypes.add(eventType.getEventName());
        return this;
    }

    public JsonNode execute() {
        log.info("📡 Fetching telemetry data from: {}", telemetryUrl);
        try {
            String jsonResponse = webClient.get()
                    .uri(telemetryUrl)
                    .header(HttpHeaders.ACCEPT, pubgUtil.getAccept())
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnNext(response -> log.info("✅ Response received (size={} bytes)", response.length()))
                    .doOnError(error -> log.error("❌ Error fetching telemetry data: ", error))
                    .block();

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                throw new RuntimeException("❌ Empty response from telemetry API");
            }

            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            log.info("✅ Successfully parsed telemetry JSON");

            // 필터링
            ArrayNode filteredEvents = objectMapper.createArrayNode();
            rootNode.forEach(node -> {
                if (node.has("_T")) {
                    String eventType = node.get("_T").asText();
                    if (eventTypes.contains(eventType)) {
                        filteredEvents.add(node);
                    }
                }
            });

            log.info("📊 Extracted {} relevant events", filteredEvents.size());
            return filteredEvents;
        } catch (Exception e) {
            log.error("❌ Exception occurred: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}

