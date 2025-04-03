package com.jkky98.spubg.service.processqueue;

import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.service.business.MatchSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@Slf4j
@RequiredArgsConstructor
public class MatchProcessingQueue {
    private final BlockingQueue<Match> queue = new LinkedBlockingQueue<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final MatchSyncService matchSyncService;

    public void addMatch(Match match) {
        try {
            queue.put(match);
            log.info("[매치 패치 작업] Match {} added to queue", match.getMatchApiId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[매치 패치 작업] Failed to add Match to queue", e);
        }
    }

    public void startProcessing() {
        for (int i = 0; i < 5; i++) {
            executorService.submit(this::processQueue);
        }
    }

    private void processQueue() {
        while (true) {
            try {
                Match match = queue.take();
                log.info("[매치 패치 작업] Processing Match: {}", match.getMatchApiId());

                matchSyncService.sync(match);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[매치 패치 작업] Worker Thread interrupted", e);
                break;
            }
        }
    }
    public synchronized int getQueueSize() {
        return queue.size();
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public boolean isQueueEmpty() {
        return queue.isEmpty();
    }

}
