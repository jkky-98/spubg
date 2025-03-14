package com.jkky98.spubg.pubg.util;

import java.util.HashMap;
import java.util.Map;

public enum GameMap {
    BALTIC_MAIN("Baltic_Main", "Erangel (Remastered)"),
    CHIMERA_MAIN("Chimera_Main", "Paramo"),
    DESERT_MAIN("Desert_Main", "Miramar"),
    DIHOR_OTOK_MAIN("DihorOtok_Main", "Vikendi"),
    ERANGEL_MAIN("Erangel_Main", "Erangel"),
    HEAVEN_MAIN("Heaven_Main", "Haven"),
    KIKI_MAIN("Kiki_Main", "Deston"),
    RANGE_MAIN("Range_Main", "Camp Jackal"),
    SAVAGE_MAIN("Savage_Main", "Sanhok"),
    SUMMERLAND_MAIN("Summerland_Main", "Karakin"),
    TIGER_MAIN("Tiger_Main", "Taego"),
    NEON_MAIN("Neon_Main", "Rondo"),
    UNKNOWN("Unknown", "Unknown"); // 기본값

    private static final Map<String, GameMap> LOOKUP = new HashMap<>();

    static {
        for (GameMap map : GameMap.values()) {
            LOOKUP.put(map.mapKey, map);
        }
    }

    private final String mapKey;
    private final String displayName;

    GameMap(String mapKey, String displayName) {
        this.mapKey = mapKey;
        this.displayName = displayName;
    }

    public static String getDisplayName(String mapKey) {
        return LOOKUP.getOrDefault(mapKey, UNKNOWN).displayName;
    }
}

