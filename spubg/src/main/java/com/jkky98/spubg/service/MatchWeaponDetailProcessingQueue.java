package com.jkky98.spubg.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.MemberMatch;
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
            log.info("MemberMatch {} added to queue", memberMatch.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to add MemberMatch to queue", e);
        }
    }

    public void startProcessing() {
        log.info("⚡ Starting MatchWeaponDetailProcessingQueue with 5 worker threads");
        for (int i = 0; i < 5; i++) {
            log.info("🛠️ Submitting worker thread {}", i + 1);
            executorService.submit(this::processQueue);
        }
        log.info("✅ All worker threads submitted successfully");
    }

    private void processQueue() {
        while (true) {
            try {
                MemberMatch memberMatch = queue.take();
                log.info("Processing start : {}", memberMatch.getId());
                memberMatchService.saveMatchWeaponDetail(memberMatch);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Worker Thread is interrupted", e);
                break;
            } catch (JsonProcessingException e) {
                Thread.currentThread().interrupt();
                log.error("Failed to process queue", e);
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
