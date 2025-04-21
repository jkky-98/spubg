package com.jkky98.spubg.discord.schedule;

public class DiscordConnectionFailureException extends RuntimeException {
    public DiscordConnectionFailureException(String message) {
        super(message);
    }
}
