package com.jkky98.spubg.pubg.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenManager {
    private final TokenBucket tokenBucket;
    private static final long MAX_WAIT_TIME_MS = 60L * 60 * 1000 * 3; // 3 hours

    public void consume() {
        synchronized (tokenBucket) {
            long startTime = System.currentTimeMillis();
            while (!tokenBucket.tryConsume()) {
                long waitedTime = System.currentTimeMillis() - startTime;
                if (waitedTime >= MAX_WAIT_TIME_MS) {
                    log.warn("[TokenManager][consume] 너무 오래 기다리고 있습니다.");
                } else {
                    log.info("[TokenManager][consume] 토큰이 없습니다 기다리는 중... ({} ms)", waitedTime);
                }

                try {
                    tokenBucket.wait(MAX_WAIT_TIME_MS - waitedTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("[TokenManager][consume] 인터럽트 발생", e);
                }
            }

            log.info("[TokenManager][consume] 토큰이 소비되었습니다. 남은 토큰: {}", tokenBucket.getAvailableTokens());
        }
    }
}
