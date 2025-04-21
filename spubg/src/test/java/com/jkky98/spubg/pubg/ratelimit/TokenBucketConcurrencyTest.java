package com.jkky98.spubg.pubg.ratelimit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TokenBucketConcurrencyTest {
    private static final int THREADS = 20;
    private static final int ATTEMPTS_PER_THREAD = 1000;

    @Test
    @DisplayName("[TokenBucket][tryConsume] 동시 다발적인 tryConsume() 호출 시에도 최대 토큰 수만큼만 성공한다")
    void concurrentConsume_doesNotOverConsume() throws InterruptedException {
        TokenBucket bucket = new TokenBucket();
        int maxTokens = bucket.getAvailableTokens();

        ExecutorService exec = Executors.newFixedThreadPool(THREADS);
        List<Future<Integer>> futures = new ArrayList<>();

        // 각 쓰레드는 ATTEMPTS_PER_THREAD번 토큰 소비 시도, 성공 횟수를 반환
        Callable<Integer> consumer = () -> {
            int success = 0;
            for (int i = 0; i < ATTEMPTS_PER_THREAD; i++) {
                if (bucket.tryConsume()) success++;
            }
            return success;
        };

        for (int i = 0; i < THREADS; i++) {
            futures.add(exec.submit(consumer));
        }
        exec.shutdown();
        exec.awaitTermination(5, TimeUnit.SECONDS);

        // 모든 쓰레드가 합쳐서 성공한 소비 횟수
        int totalConsumed = futures.stream().mapToInt(f -> {
            try { return f.get(); }
            catch(Exception e) { throw new RuntimeException(e); }
        }).sum();

        // 최대 허용치 이상 소비되지 않았어야 함
        Assertions.assertThat(totalConsumed).isEqualTo(maxTokens);
    }
}
