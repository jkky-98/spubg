package com.jkky98.spubg.discord.domain;

import lombok.Data;

@Data
public class GrenadeRanking {
    private String username;
    private Integer totalGrenadeCount;
    private Double totalGrenadeDamage;
    private Double weightedScore;
    private Double avgDamagePerGrenade;
    private Integer ranking;
}
