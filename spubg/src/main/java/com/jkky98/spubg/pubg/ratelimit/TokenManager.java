package com.jkky98.spubg.pubg.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenManager {
    private final TokenBucket tokenBucket;
    private static final long MAX_WAIT_TIME_MS = 60L * 60 * 1000 * 3;

    public void consume() {
        synchronized (tokenBucket) {
            long startTime = System.currentTimeMillis();
            while (!tokenBucket.tryConsume()) {
                long waitedTime = System.currentTimeMillis() - startTime;
                if (waitedTime >= MAX_WAIT_TIME_MS) {
                    log.warn("[TokenManager] Waited too long. Still no token.");
                } else {
                    log.info("[TokenManager] No tokens. Waiting... ({} ms)", waitedTime);
                }

                try {
                    tokenBucket.wait(MAX_WAIT_TIME_MS - waitedTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted", e);
                }
            }

            log.info("[TokenManager] Token consumed. Remaining: {}", tokenBucket.getAvailableTokens());
        }
    }
}
