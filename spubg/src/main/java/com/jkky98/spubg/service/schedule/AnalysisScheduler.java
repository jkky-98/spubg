package com.jkky98.spubg.service.schedule;

import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.MemberMatch;
import com.jkky98.spubg.service.implement.MatchReader;
import com.jkky98.spubg.service.implement.MemberMatchReader;
import com.jkky98.spubg.service.processqueue.MatchProcessingQueue;
import com.jkky98.spubg.service.processqueue.MatchWeaponDetailProcessingQueue;
import com.jkky98.spubg.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalysisScheduler {
    private final MatchProcessingQueue matchProcessingQueue;
    private final MatchWeaponDetailProcessingQueue matchWeaponDetailProcessingQueue;
    private final MemberService memberService;
    private final MatchReader matchReader;
    private final MemberMatchReader memberMatchReader;

    @Scheduled(fixedRate = 60000 * 10)
    public void fetchProcessMatches() {
        log.debug("[AnalysisScheduler][fetchProcessMatches] 스케줄링 시작");
        List<Match> matches = matchReader.readByBoolIsAnalysisFalse();

        if (matches.isEmpty()) {
            log.debug("[AnalysisScheduler][fetchProcessMatches] 작업할 매치가 존재하지 않습니다. 작업을 패싱합니다.");
            return;
        }

        matches.forEach(matchProcessingQueue::addMatch);
        log.debug("[AnalysisScheduler][fetchProcessMatches] 작업 추가 :: {}개의 매치가 작업큐에 추가되었습니다.", matches.size());
    }

    @Scheduled(fixedRate = 60000 * 10)
    public void fetchProcessMatchWeaponDetail() {
        log.debug("[AnalysisScheduler][fetchProcessMatchWeaponDetail] 스케줄링 시작");
        List<MemberMatch> memberMatchNeedToAnaysis = memberMatchReader.getMemberMatchNeedToAnaysis();

        if (memberMatchNeedToAnaysis.isEmpty()) {
            log.debug("[AnalysisScheduler][fetchProcessMatchWeaponDetail] 작업할 딜량 데이터 추출 작업이 존재하지 않습니다. 작업을 패싱합니다.");
            return;
        }

        memberMatchNeedToAnaysis.forEach(matchWeaponDetailProcessingQueue::addMemberMatch);
        log.debug("[AnalysisScheduler][fetchProcessMatchWeaponDetail] 작업 추가 :: {}개의 매치가 작업큐에 추가되었습니다.", memberMatchNeedToAnaysis.size());
    }

    @Scheduled(fixedRate = 60000 * 10)
    public void fetchProcessMember() {
        log.debug("[AnalysisScheduler][fetchProcessMember] 스케줄링 시작");
        memberService.fetchMember();
    }
}



