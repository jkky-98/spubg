package com.jkky98.spubg.service.processqueue;

import com.jkky98.spubg.domain.Match;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public void addMatch(Match match) {
        try {
            queue.put(match);
            log.debug("[매치 패치 작업] Match {} added to queue", match.getMatchApiId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[매치 패치 작업] Failed to add Match to queue", e);
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
            matchQueueWorker.process(queue, running);  // @Async 메서드 호출
        }
        log.debug("MatchProcessingQueue started with {} workers", workerCount);
    }

    @Override
    public void stop() {
        running.set(false);
        log.debug("MatchProcessingQueue is stopping.");
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int getPhase() {
        return 1000;
    }
}
