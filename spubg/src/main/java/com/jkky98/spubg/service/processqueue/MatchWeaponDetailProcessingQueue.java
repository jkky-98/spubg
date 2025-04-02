package com.jkky98.spubg.service.processqueue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jkky98.spubg.domain.MemberMatch;
import com.jkky98.spubg.service.MemberMatchService;
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
public class MatchWeaponDetailProcessingQueue {

    private final BlockingQueue<MemberMatch> queue = new LinkedBlockingQueue<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final MemberMatchService memberMatchService;

    public void addMemberMatch(MemberMatch memberMatch) {
        try {
            queue.put(memberMatch);
            log.info("[텔레메트리 패치 작업] MemberMatch {} added to queue", memberMatch.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[텔레메트리 패치 작업] Failed to add MemberMatch to queue", e);
        }
    }

    public void startProcessing() {
        log.info("[텔레메트리 패치 작업] ⚡ Starting MatchWeaponDetailProcessingQueue with 5 worker threads");
        for (int i = 0; i < 5; i++) {
            log.info("[텔레메트리 패치 작업] 🛠️ Submitting worker thread {}", i + 1);
            executorService.submit(this::processQueue);
        }
        log.info("[텔레메트리 패치 작업] ✅ All worker threads submitted successfully");
    }

    private void processQueue() {
        while (true) {
            try {
                MemberMatch memberMatch = queue.take();
                log.info("[텔레메트리 패치 작업] Processing start : {}", memberMatch.getId());
                memberMatchService.saveMatchWeaponDetail(memberMatch);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[텔레메트리 패치 작업] Worker Thread is interrupted", e);
                break;
            } catch (JsonProcessingException e) {
                Thread.currentThread().interrupt();
                log.error("[텔레메트리 패치 작업] Failed to process queue", e);
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
