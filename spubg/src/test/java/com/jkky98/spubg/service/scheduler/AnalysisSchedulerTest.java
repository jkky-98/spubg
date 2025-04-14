package com.jkky98.spubg.service.scheduler;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.MemberMatch;
import com.jkky98.spubg.service.SeasonService;
import com.jkky98.spubg.service.implement.MatchReader;
import com.jkky98.spubg.service.implement.MemberMatchReader;
import com.jkky98.spubg.service.processqueue.MatchProcessingQueue;
import com.jkky98.spubg.service.processqueue.MatchWeaponDetailProcessingQueue;
import com.jkky98.spubg.service.MemberService;
import com.jkky98.spubg.service.schedule.AnalysisScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AnalysisSchedulerTest {

    @Mock
    private MatchProcessingQueue matchProcessingQueue;

    @Mock
    private MatchWeaponDetailProcessingQueue matchWeaponDetailProcessingQueue;

    @Mock
    private MemberService memberService;

    @Mock
    private MatchReader matchReader;

    @Mock
    private MemberMatchReader memberMatchReader;

    @Mock
    private SeasonService seasonService;

    @InjectMocks
    private AnalysisScheduler analysisScheduler;

    @Test
    @DisplayName("[AnalysisScheduler][fetchProcessMatches] 매치가 있을 경우 작업큐에 매치가 추가되어야 함")
    void testFetchProcessMatchesWithMatches() {
        // given
        Match match1 = Match.builder().build();
        Match match2 = Match.builder().build();
        List<Match> matches = Arrays.asList(match1, match2);
        when(matchReader.readByBoolIsAnalysisFalse()).thenReturn(matches);

        // when
        analysisScheduler.fetchProcessMatches();

        // then
        verify(matchProcessingQueue).addMatch(match1);
        verify(matchProcessingQueue).addMatch(match2);
    }

    @Test
    @DisplayName("[AnalysisScheduler][fetchProcessMatches] 매치가 없으면 작업큐 추가를 하지 않아야 함")
    void testFetchProcessMatchesNoMatches() {
        // given
        when(matchReader.readByBoolIsAnalysisFalse()).thenReturn(Collections.emptyList());

        // when
        analysisScheduler.fetchProcessMatches();

        // then
        verify(matchProcessingQueue, never()).addMatch(any());
    }

    @Test
    @DisplayName("[AnalysisScheduler][fetchProcessMatchWeaponDetail] 분석할 MemberMatch가 있을 경우 작업큐에 추가되어야 함")
    void testFetchProcessMatchWeaponDetailWithMatches() {
        // given
        MemberMatch memberMatch1 = MemberMatch.builder().build();
        MemberMatch memberMatch2 = MemberMatch.builder().build();
        List<MemberMatch> memberMatches = Arrays.asList(memberMatch1, memberMatch2);
        when(memberMatchReader.getMemberMatchNeedToAnaysis()).thenReturn(memberMatches);

        // when
        analysisScheduler.fetchProcessMatchWeaponDetail();

        // then
        verify(matchWeaponDetailProcessingQueue).addMemberMatch(memberMatch1);
        verify(matchWeaponDetailProcessingQueue).addMemberMatch(memberMatch2);
    }

    @Test
    @DisplayName("[AnalysisScheduler][fetchProcessMatchWeaponDetail] 분석할 MemberMatch가 없으면 작업큐 추가를 하지 않아야 함")
    void testFetchProcessMatchWeaponDetailNoMatches() {
        // given
        when(memberMatchReader.getMemberMatchNeedToAnaysis()).thenReturn(Collections.emptyList());

        // when
        analysisScheduler.fetchProcessMatchWeaponDetail();

        // then: 작업큐에 추가하는 메서드가 호출되지 않아야 함
        verify(matchWeaponDetailProcessingQueue, never()).addMemberMatch(any());
    }

    @Test
    @DisplayName("[AnalysisScheduler][fetchProcessMember] memberService.fetchMember 호출 여부")
    void testFetchProcessMember() {
        // when
        analysisScheduler.fetchProcessMember();

        // then
        verify(memberService).fetchMember();
    }

    @Test
    @DisplayName("[AnalysisScheduler][fetchSeason] seasonService.updateSeason 호출 여부")
    void testFetchSeason() {
        // when
        analysisScheduler.fetchSeason();

        // then
        verify(seasonService).updateSeason();
    }
}
