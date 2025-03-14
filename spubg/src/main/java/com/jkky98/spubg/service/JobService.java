package com.jkky98.spubg.service;

import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {
    private final MatchRepository matchRepository;
    private final MatchProcessingQueue matchProcessingQueue;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public synchronized void fetchAndProcessMatches() {
        // ✅ 기존 작업이 남아있으면 실행하지 않음
        if (!matchProcessingQueue.isQueueEmpty()) {
            log.info("Queue is not empty. Skipping this execution.");
            return;
        }

        log.info("Fetching matches to process...");
        List<Match> matches = matchRepository.findByBoolIsAnalysisFalse();

        if (matches.isEmpty()) {
            log.info("No matches found for processing.");
            return;
        }

        log.info("Adding {} matches to queue", matches.size());
        matches.forEach(matchProcessingQueue::addMatch);

        matchProcessingQueue.startProcessing(); // ✅ 작업 시작
    }
}


