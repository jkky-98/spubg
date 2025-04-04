package com.jkky98.spubg.pubg.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

import static com.jkky98.spubg.pubg.ratelimit.TokenBucketConst.MAX_TOKENS;

@Component
@Slf4j
public class TokenBucket {
    private final Semaphore tokens = new Semaphore(MAX_TOKENS);

    public synchronized boolean tryConsume() {
        return tokens.tryAcquire();
    }

    public synchronized void refill() {
        if (tokens.availablePermits() < MAX_TOKENS) {
            tokens.release();
            log.debug("[TokenBucket][refill] 🆕 토큰 추가 (현재 토큰 수: {}/{})", tokens.availablePermits(), MAX_TOKENS);

            notify();
        }
    }

    public int getAvailableTokens() {
        return tokens.availablePermits();
    }
}
