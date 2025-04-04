package com.jkky98.spubg.service.processqueue;

import com.fasterxml.jackson.databind.JsonNode;
import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.pubg.request.PubgApiRequestService;
import com.jkky98.spubg.service.business.MatchSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
@RequiredArgsConstructor
public class MatchQueueWorker {
    private final PubgApiRequestService pubgApiRequestService;
    private final MatchSyncService matchSyncService;

    @Async
    public void process(BlockingQueue<Match> queue, AtomicBoolean running) {
        while (running.get()) {
            try {
                // 1초 간격으로 큐 폴링
                Match match = queue.poll(1, TimeUnit.SECONDS);
                if (match != null) {
                    log.debug("[매치 패치 작업] Processing Match: {}", match.getMatchApiId());
                    /**
                     * 외부 API 연결
                     */
                    JsonNode rootNode = pubgApiRequestService.requestMatch(match.getMatchApiId());
                    matchSyncService.sync(match, rootNode);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Worker Thread interrupted", e);
                break;
            }
        }
    }
}
