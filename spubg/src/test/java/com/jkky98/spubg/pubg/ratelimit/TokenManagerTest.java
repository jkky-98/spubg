package com.jkky98.spubg.pubg.ratelimit;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TokenManager.consume() 의 모든 분기(즉시 소비, 대기 후 소비, 인터럽트 예외)를
 * 실제 TokenBucket 인스턴스를 사용하여 검증합니다.
 */
class TokenManagerTest {

    private TokenBucket bucket;
    private TokenManager manager;

    @BeforeEach
    void setUp() {
        bucket = new TokenBucket();
        manager = new TokenManager(bucket);
    }

    @Test
    @DisplayName("[TokenManager][consume] 즉시 소비: 토큰이 충분할 때 바로 1개 감소")
    void consume_immediateDecrement() {
        int initial = bucket.getAvailableTokens();
        manager.consume();
        assertThat(bucket.getAvailableTokens()).isEqualTo(initial - 1);
    }


    @Test
    @DisplayName("[TokenManager][consume] 토큰 부족 시 대기 후 refill() 호출로 소비 완료")
    void consume_waitThenConsumeAfterRefill() throws InterruptedException {
        int initial = bucket.getAvailableTokens();
        for (int i = 0; i < initial; i++) {
            assertThat(bucket.tryConsume()).isTrue();
        }
        assertThat(bucket.getAvailableTokens()).isZero();

        Thread consumer = new Thread(manager::consume);
        consumer.start();

        Thread.sleep(100);
        assertThat(consumer.isAlive()).isTrue();

        bucket.refill();

        consumer.join(500);
        assertThat(consumer.isAlive()).isFalse();

        assertThat(bucket.getAvailableTokens()).isZero();
    }

    @Test
    @DisplayName("[TokenManager][consume] consume() 대기 중 인터럽트 발생 시 RuntimeException 발생")
    void consume_interruptThrows() throws Exception {
        int initial = bucket.getAvailableTokens();
        for (int i = 0; i < initial; i++) {
            assertThat(bucket.tryConsume()).isTrue();
        }
        assertThat(bucket.getAvailableTokens()).isZero();

        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch done    = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Thread t = new Thread(() -> {
            started.countDown();
            try {
                manager.consume();
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        t.start();

        started.await(1, TimeUnit.SECONDS);
        Thread.sleep(100);

        t.interrupt();

        assertThat(done.await(1, TimeUnit.SECONDS)).isTrue();

        Throwable ex = error.get();
        assertThat(ex).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("인터럽트 발생");
    }

    @DisplayName("[TokenManager][consume] 토큰 부족 시 Timeout 후 TokenUnavailavleException 발생")
    @Test
    void consume_timeoutThrowsWithMockito() throws InterruptedException {
        // 1) 모킹 버킷 준비
        TokenBucket mockBucket = mock(TokenBucket.class);
        when(mockBucket.tryConsume(anyLong(), any(TimeUnit.class))).thenReturn(false);

        TokenManager manager = new TokenManager(mockBucket);

        // 2) 예외 분기 검증
        assertThatThrownBy(manager::consume)
                .isInstanceOf(TokenUnavailavleException.class)
                .hasMessageContaining("토큰을")
                .hasMessageContaining("ms");
    }
}
