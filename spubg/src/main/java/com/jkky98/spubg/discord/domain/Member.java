package com.jkky98.spubg.discord.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Member {
    private Long memberId;
    private String username;
    private String accountId;
    private String discordName;
}
