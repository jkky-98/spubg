package com.jkky98.spubg.service.processqueue;

import com.jkky98.spubg.domain.Match;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
@RequiredArgsConstructor
public class MatchProcessingQueue implements SmartLifecycle {
    private final BlockingQueue<Match> queue = new LinkedBlockingQueue<>();
    private final MatchQueueWorker matchQueueWorker;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final int workerCount = 5;

    @Value("${match-processing.auto-startup:true}")
    private boolean autoStartup;

    public void addMatch(Match match) {
        try {
            // toDo : queue.offer(Match, 3(시간), 시간단위(TimeUnit.SECONDS) -> 큐 put 실패에 대한 최대 대기시간 설정 가능 실패시 false -> 예외 처리
            queue.put(match);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized int getQueueSize() {
        return queue.size();
    }

    public boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    @Override
    public void start() {
        running.set(true);
        for (int i = 0; i < workerCount; i++) {
            matchQueueWorker.process(queue, running);
        }
        log.debug("[MatchProcessingQueue][start] 멀티쓰레드 워커 시작 - 워커 수 : {}", workerCount);
    }

    @Override
    public void stop() {
        running.set(false);
        log.debug("[MatchProcessingQueue][stop] MatchProcessingQueue 중지");
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int getPhase() {
        return 1000;
    }

    @Override
    public boolean isAutoStartup() {
        return autoStartup;
    }
}
