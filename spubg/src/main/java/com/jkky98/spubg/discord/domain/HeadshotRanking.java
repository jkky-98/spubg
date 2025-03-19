package com.jkky98.spubg.discord.domain;

import lombok.Data;

@Data
public class HeadshotRanking {
    private Long memberId;
    private String username;
    private int headshotCount;
    private int totalDamageCount;
    private double headshotRatio;
    private int ranking;
}

