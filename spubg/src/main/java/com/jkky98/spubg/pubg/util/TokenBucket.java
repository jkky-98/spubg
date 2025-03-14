package com.jkky98.spubg.pubg.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

import static com.jkky98.spubg.pubg.util.TokenBucketConst.MAX_TOKENS;

@Component
public class TokenBucket {
    private final Semaphore tokens = new Semaphore(MAX_TOKENS);

    public synchronized boolean tryConsume() {
        return tokens.tryAcquire();
    }

    public synchronized void refill() {
        if (tokens.availablePermits() < MAX_TOKENS) {
            tokens.release();
            notify(); // 🔹 대기 중인 요청 깨우기
        }
    }
}
