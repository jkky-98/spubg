package com.jkky98.spubg.discord.domain;

import lombok.Data;

@Data
public class GroggyRanking {
    private String username;
    private int totalMatches;
    private double groggyRatio;
    private int ranking;
}