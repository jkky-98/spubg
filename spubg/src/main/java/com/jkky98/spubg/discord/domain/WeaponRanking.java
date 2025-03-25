package com.jkky98.spubg.discord.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WeaponRanking {
    private String username;
    private String weaponName;
    private BigDecimal totalDamage;
    private int match;
    private BigDecimal perMatch;
    private BigDecimal perGroggy;
    private BigDecimal totalScore;
    private int ranking;
}
