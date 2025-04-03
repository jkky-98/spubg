package com.jkky98.spubg.pubg.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jkky98.spubg.pubg.enums.TelemetryEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Scope("prototype")
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
        log.info("[Telemetry Fetch] Try fetching telemetry data from: {}", telemetryUrl);
        try {
            String jsonResponse = webClient.get()
                    .uri(telemetryUrl)
                    .header(HttpHeaders.ACCEPT, pubgUtil.getAccept())
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnNext(response -> log.info("[Telemetry Fetch] ✅ Response received with webClient (size={} bytes)", response.length()))
                    .doOnError(error -> log.error("[Telemetry Fetch] ❌ Error fetching telemetry data Response received with webClient: ", error))
                    .block();

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                throw new RuntimeException("[Telemetry Fetch] ❌ Empty or null json response from telemetry API");
            }

            ArrayNode rootArray = (ArrayNode) objectMapper.readTree(jsonResponse);
            log.info("[Telemetry Fetch] ✅ Successfully parsed telemetry JSON");

            ArrayNode filteredEvents = objectMapper.createArrayNode();
            rootArray.forEach(node -> {
                String eventType = node.path("_T").asText(null);
                if (eventTypes.contains(eventType)) {
                    filteredEvents.add(node);
                }
            });

            log.info("[Telemetry Fetch] 📊 Extracted {} relevant events", filteredEvents.size());
            return filteredEvents;
        } catch (Exception e) {
            log.error("[Telemetry Fetch] ❌ Exception occurred: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}

