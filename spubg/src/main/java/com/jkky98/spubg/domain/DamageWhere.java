package com.jkky98.spubg.domain;

import java.util.HashMap;
import java.util.Map;

public enum DamageWhere {
    NONSPECIFIED("NonSpecific"),
    HEADSHOT("HeadShot"),
    LEGSHOT("LegShot"),
    PELVISSHOT("PelvisShot"),
    ARMSHOT("ArmShot"),
    TORSOSHOT("TorsoShot"),
    NONE("None");

    private static final Map<String, DamageWhere> REASON_MAP = new HashMap<>();
    private final String displayName;

    static {
        REASON_MAP.put("NonSpecific", NONSPECIFIED);
        REASON_MAP.put("HeadShot", HEADSHOT);
        REASON_MAP.put("LegShot", LEGSHOT);
        REASON_MAP.put("PelvisShot", PELVISSHOT);
        REASON_MAP.put("ArmShot", ARMSHOT);
        REASON_MAP.put("TorsoShot", TORSOSHOT);
        REASON_MAP.put("None", NONE);
    }

    DamageWhere(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DamageWhere fromkey(String damageReason) {
        return REASON_MAP.getOrDefault(damageReason, NONE);
    }
}
