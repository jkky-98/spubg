package com.jkky98.spubg.pubg.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenManager {
    private static final long MAX_WAIT_TIME_MS = 1_000;
    private final TokenBucket bucket;

    public void consume() {
        try {
            boolean acquired = bucket.tryConsume(MAX_WAIT_TIME_MS, TimeUnit.MILLISECONDS);
            if (!acquired) {
                log.debug("[TokenManager][consume] 토큰 대기시간 초과 ({} ms)", MAX_WAIT_TIME_MS);
                throw new TokenUnavailavleException("토큰을 " + MAX_WAIT_TIME_MS + "ms 안에 확보하지 못했습니다");
            } else {
                log.debug("[TokenManager][consume] 토큰 소비되었습니다. 남은 토큰: {}",
                        bucket.getAvailableTokens());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("[TokenManager][consume] 인터럽트 발생", e);
        }
    }
}
