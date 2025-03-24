package com.jkky98.spubg.pubg.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

import static com.jkky98.spubg.pubg.util.TokenBucketConst.MAX_TOKENS;

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
            log.info("[토큰 버킷] 🆕 New token added! (Current tokens: {}/{})", tokens.availablePermits(), MAX_TOKENS);

            notify(); // 🔹 대기 중인 요청 깨우기
            log.info("[토큰 버킷] 🔔 Notified waiting threads.");
        }
    }

    public int getAvailableTokens() {
        return tokens.availablePermits();
    }
}
