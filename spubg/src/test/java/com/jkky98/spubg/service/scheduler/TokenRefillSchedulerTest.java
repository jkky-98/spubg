package com.jkky98.spubg.service.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import com.jkky98.spubg.pubg.ratelimit.TokenBucket;
import com.jkky98.spubg.service.schedule.TokenRefillScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TokenRefillSchedulerTest {

    @Mock
    private TokenBucket tokenBucket;

    @InjectMocks
    private TokenRefillScheduler tokenRefillScheduler;

    @Test
    @DisplayName("[TokenRefillScheduler][refillTokens] refill() 메서드가 호출되었는지 검증")
    void testRefillTokens() {
        // when
        tokenRefillScheduler.refillTokens();

        // then
        verify(tokenBucket, times(1)).refill();
    }
}
