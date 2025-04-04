package com.jkky98.spubg.pubg.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PubgApiRequestService {

    private final PubgApiManager pubgApiManager;

    public JsonNode requestSeason() {
        return pubgApiManager.get("/seasons");
    }

    public JsonNode requestMember(String username) {
        return pubgApiManager.get("/players?filter[playerNames]=" + username);
    }

    public JsonNode requestMatch(String matchId) {
        return pubgApiManager.get("/matches/" + matchId);
    }

    public JsonNode requestManyMembers(List<String> names) {
        String joined = String.join(",", names);
        return pubgApiManager.get("/players?filter[playerNames]=" + joined);
    }
}
