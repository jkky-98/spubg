package com.jkky98.spubg.service.schedule;

import com.jkky98.spubg.pubg.ratelimit.TokenBucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRefillService {

    private final TokenBucket tokenBucket;

    @Scheduled(fixedRate = 7000) // 7초에 하나씩 토큰 리필 (토큰 최고 용량 10)
    public void refillTokens() {
        tokenBucket.refill();
    }
}
