package com.jkky98.spubg.service.processqueue;

import com.jkky98.spubg.domain.MemberMatch;
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
public class MatchWeaponDetailProcessingQueue implements SmartLifecycle {

    private final BlockingQueue<MemberMatch> queue = new LinkedBlockingQueue<>();
    private final MatchWeaponDetailQueueWorker matchWeaponDetailQueueWorker;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final int workerCount = 5;

    public void addMemberMatch(MemberMatch memberMatch) {
        try {
            queue.put(memberMatch);
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
            matchWeaponDetailQueueWorker.process(queue, running);
        }
        log.debug("[MatchWeaponDetailProcessingQueue][start] started with {} workers", workerCount);
    }

    @Override
    public void stop() {
        running.set(false);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int getPhase() {
        return 1001;
    }
}
