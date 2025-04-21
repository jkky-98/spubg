package com.jkky98.spubg.pubg.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenBucketTest {

    private TokenBucket tokenBucket;
    private int maxTokens;

    @BeforeEach
    void setUp() {
        tokenBucket = new TokenBucket();
        maxTokens = tokenBucket.getAvailableTokens();
    }

    @Test
    @DisplayName("[TokenBucket][getAvailableTokens] 초기 상태 : 토큰이 가득 참")
    void testGetAvailableTokens() {
        // given
        // when
        // then
        assertThat(tokenBucket.getAvailableTokens()).isEqualTo(maxTokens);
    }

    @Test
    @DisplayName("[TokenBucket][tryConsume] 토큰 하나 소비")
    void testTryConsume() {
        // given
        // when
        boolean isConsumed = tokenBucket.tryConsume();

        // then
        assertThat(isConsumed).isTrue();
        assertThat(tokenBucket.getAvailableTokens()).isEqualTo(maxTokens - 1);
    }

    @Test
    @DisplayName("[TokenBucket][tryConsume] 토큰 모두 소비 -> 소비 시도시 소비 불가능")
    void testTryConsumeException() {
        // given
        // when
        // 모든 토큰 소진
        for (int i = 0; i < maxTokens; i++) {
            assertThat(tokenBucket.tryConsume()).isTrue();
        }

        // then
        // 소진된 상태에서 소비 시도
        assertThat(tokenBucket.tryConsume()).isFalse();
        assertThat(tokenBucket.getAvailableTokens()).isEqualTo(0);
    }

    @Test
    @DisplayName("[TokenBucket][refill]: 비어있는 상태에서 호출시 토큰 하나 추가")
    void testRefill() {
        // given
        for (int i = 0; i < maxTokens; i++) {
            tokenBucket.tryConsume();
        }

        // when
        tokenBucket.refill();

        // then
        assertThat(tokenBucket.getAvailableTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("[TokenBucket][refill]: 꽉찬 상태에서 리필 시도")
    void testRefillFull() {
        tokenBucket.refill();
        tokenBucket.refill();

        assertThat(tokenBucket.getAvailableTokens()).isEqualTo(maxTokens);
    }
}
