package com.jkky98.spubg.service;

import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.MemberMatch;
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
    private final MemberMatchService memberMatchService;
    private final MatchProcessingQueue matchProcessingQueue;
    private final MatchWeaponDetailProcessingQueue matchWeaponDetailProcessingQueue;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public synchronized void fetchAndProcessMatches() {
        // ✅ 기존 작업이 남아있으면 실행하지 않음
        if (!matchProcessingQueue.isQueueEmpty()) {
            log.info("Queue is not empty. Skipping this execution. : matchProcessingQueue");
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

    @Scheduled(fixedRate = 60000)
    public synchronized void fetchAndProcessMatchWeaponDetail() {
        // ✅ 기존 작업이 남아있으면 실행하지 않음
        if (!matchWeaponDetailProcessingQueue.isQueueEmpty()) {
            log.info("[fetchAndProcessMatchWeaponDetail] Queue is not empty. Skipping this execution. : matchWeaponDetailProcessingQueue");
            return;
        }

        List<MemberMatch> memberMatchNeedToAnaysis = memberMatchService.getMemberMatchNeedToAnaysis();

        if (memberMatchNeedToAnaysis.isEmpty()) {
            log.info("[fetchAndProcessMatchWeaponDetail] No matches found for processing. Skipping this execution. : memberMatchNeedToAnaysis");
            return;
        }

        log.info("[fetchAndProcessMatchWeaponDetail] Adding {} MemberMatches to queue", memberMatchNeedToAnaysis.size());
        memberMatchNeedToAnaysis.forEach(matchWeaponDetailProcessingQueue::addMemberMatch);

        log.info("[fetchAndProcessMatchWeaponDetail] StartProcessing : " + memberMatchNeedToAnaysis.size());
        matchWeaponDetailProcessingQueue.startProcessing(); // 작업 시작
    }
}


