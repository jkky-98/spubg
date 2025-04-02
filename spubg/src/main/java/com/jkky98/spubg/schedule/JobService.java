package com.jkky98.spubg.schedule;

import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.MemberMatch;
import com.jkky98.spubg.repository.MatchRepository;
import com.jkky98.spubg.service.processqueue.MatchProcessingQueue;
import com.jkky98.spubg.service.processqueue.MatchWeaponDetailProcessingQueue;
import com.jkky98.spubg.service.MemberMatchService;
import com.jkky98.spubg.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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
    private final MemberService memberService;

    @Scheduled(fixedRate = 60000 * 10) // 1분마다 실행
    @Async
    public void fetchAndProcessMatches() {
        int remainingTasks = matchProcessingQueue.getQueueSize();

        if (remainingTasks > 0) {
            log.info("[매치 패치 작업]✅✅✅ 기존 매치 프로세스 작업이 남아있습니다. 남은 작업: {}개. 작업을 패싱합니다. ✅✅✅", remainingTasks);
            return;
        }

        log.info("[매치 패치 작업] Fetching matches to process...");
        List<Match> matches = matchRepository.findByBoolIsAnalysisFalse();

        if (matches.isEmpty()) {
            log.info("[매치 패치 작업] ✅✅✅ 작업할 매치가 존재하지 않습니다. 작업을 패싱합니다. ✅✅✅");
            return;
        }

        log.info("[매치 패치 작업] Adding {} matches to queue", matches.size());
        matches.forEach(matchProcessingQueue::addMatch);

        matchProcessingQueue.startProcessing(); // ✅ 작업 시작
    }

    @Scheduled(fixedRate = 60000 * 3)
    @Async
    public void fetchAndProcessMatchWeaponDetail() {
        int remainingTasks = matchWeaponDetailProcessingQueue.getQueueSize();

        if (remainingTasks > 0) {
            log.info("[텔레메트리 패치 작업]✅✅✅ 기존 딜량 추출 프로세스 작업이 남아있습니다. 남은 작업: {}개. 작업을 패싱합니다. ✅✅✅", remainingTasks);
            return;
        }

        List<MemberMatch> memberMatchNeedToAnaysis = memberMatchService.getMemberMatchNeedToAnaysis();

        if (memberMatchNeedToAnaysis.isEmpty()) {
            log.info("[텔레메트리 패치 작업] ✅✅✅ 작업할 딜량 데이터 추출 작업이 존재하지 않습니다. 작업을 패싱합니다. ✅✅✅");
            return;
        }

        log.info("[텔레메트리 패치 작업] Adding {} MemberMatches to queue", memberMatchNeedToAnaysis.size());
        memberMatchNeedToAnaysis.forEach(matchWeaponDetailProcessingQueue::addMemberMatch);

        log.info("[텔레메트리 패치 작업] StartProcessing : {}", memberMatchNeedToAnaysis.size());
        matchWeaponDetailProcessingQueue.startProcessing(); // 작업 시작
    }

    @Scheduled(fixedRate = 60000 * 10)
    @Async
    public void fetchAndProcessMember() {
        memberService.fetchMember();
    }
}



