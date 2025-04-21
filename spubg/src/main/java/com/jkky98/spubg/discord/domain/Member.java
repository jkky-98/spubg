package com.jkky98.spubg.discord.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Member {
    private Long memberId;
    private String username;
    private String accountId;
    private String discordName;
}
