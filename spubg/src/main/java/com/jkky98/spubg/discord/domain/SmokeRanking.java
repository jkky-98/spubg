package com.jkky98.spubg.discord.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SmokeRanking {
    private String username;
    private String weaponName;
    private BigDecimal perMatch;
    private int ranking;
}
