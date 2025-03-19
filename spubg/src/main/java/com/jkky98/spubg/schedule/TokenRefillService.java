package com.jkky98.spubg.schedule;

import com.jkky98.spubg.pubg.util.TokenBucket;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenRefillService {

    private final TokenBucket tokenBucket;

    @Async
    @Scheduled(fixedRate = 7000) // 7초에 하나씩 토큰 리필 (토큰 최고 용량 10)
    public void refillTokens() {
        tokenBucket.refill();
    }
}
