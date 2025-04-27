package com.jkky98.spubg.pubg.ratelimit;

public class TokenUnavailavleException extends RuntimeException {
    public TokenUnavailavleException(String message) {
        super(message);
    }
}
