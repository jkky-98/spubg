package com.jkky98.spubg.discord.domain;

import lombok.Data;

@Data
public class PhaseDealtRanking {
    private String username;
    private double avgDealt;
    private double weightDamage;
    private int ranking;
}
