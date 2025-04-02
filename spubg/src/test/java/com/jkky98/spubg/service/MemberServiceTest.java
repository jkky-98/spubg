package com.jkky98.spubg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkky98.spubg.domain.GameMode;
import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.Member;
import com.jkky98.spubg.pubg.request.PubgApiRequestService;
import com.jkky98.spubg.repository.MatchRepository;
import com.jkky98.spubg.repository.MemberMatchRepository;
import com.jkky98.spubg.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MemberMatchRepository memberMatchRepository;

    @Mock
    private PubgApiRequestService pubgApiRequestService;

    @InjectMocks
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void fetchMember_shouldSaveMatchAndMemberMatch_whenNewMatchExists() throws Exception {
        // Given
        Member member = Member.builder().username("tester").accountId("acc-1").build();
        when(memberRepository.findAll()).thenReturn(Collections.singletonList(member));
        when(memberRepository.findByAccountId("acc-1")).thenReturn(Optional.of(member));

        String json = "{\"data\": [ {\"id\": \"acc-1\", \"relationships\": {\"matches\": {\"data\": [ {\"id\": \"match-1\"} ] } } } ] }";
        JsonNode mockNode = new ObjectMapper().readTree(json);
        when(pubgApiRequestService.requestManyMembers(anyList())).thenReturn(mockNode);

        when(matchRepository.countByMatchApiIdIn(anyList())).thenReturn(0L);
        when(matchRepository.existsByMatchApiId("match-1")).thenReturn(false);
        when(matchRepository.saveAll(anyList())).thenAnswer(i -> i.getArguments()[0]);
        when(memberMatchRepository.saveAll(anyList())).thenAnswer(i -> i.getArguments()[0]);

        // When
        memberService.fetchMember();

        // Then
        verify(matchRepository, times(1)).saveAll(anyList());
        verify(memberMatchRepository, times(1)).saveAll(anyList());
    }

    @Test
    void fetchMember_shouldReturnEarly_whenAllMatchesExist() throws Exception {
        // Given
        Member member = Member.builder().username("tester").accountId("acc-1").build();
        when(memberRepository.findAll()).thenReturn(Collections.singletonList(member));
        String json = "{\"data\": [ {\"id\": \"acc-1\", \"relationships\": {\"matches\": {\"data\": [ {\"id\": \"match-1\"} ] } } } ] }";
        JsonNode mockNode = new ObjectMapper().readTree(json);
        when(pubgApiRequestService.requestManyMembers(anyList())).thenReturn(mockNode);

        when(matchRepository.countByMatchApiIdIn(anyList())).thenReturn(1L); // match already exists

        // When
        memberService.fetchMember();

        // Then
        verify(matchRepository, never()).saveAll(anyList());
        verify(memberMatchRepository, never()).saveAll(anyList());
    }

    @Test
    void checkFetchable_shouldReturnTrue_whenAllMatchIdsExist() throws Exception {
        // given
        String json = "{\"data\": [ {\"id\": \"acc-1\", \"relationships\": {\"matches\": {\"data\": [ {\"id\": \"match-1\"}, {\"id\": \"match-2\"} ] } } } ] }";
        JsonNode dataNode = new ObjectMapper().readTree(json).get("data");
        when(matchRepository.countByMatchApiIdIn(argThat(list ->
                list.containsAll(Arrays.asList("match-1", "match-2")) && list.size() == 2
        ))).thenReturn(2L);

        // when
        boolean result = memberServiceTestable.checkFetchable(dataNode);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void setMemberMatchMap_shouldReturnCorrectMap() throws Exception {
        // given
        Member member = Member.builder().username("tester").accountId("acc-1").build();
        when(memberRepository.findByAccountId("acc-1")).thenReturn(Optional.of(member));

        String json = "{\"data\": [ {\"id\": \"acc-1\", \"relationships\": {\"matches\": {\"data\": [ {\"id\": \"match-1\"}, {\"id\": \"match-2\"} ] } } } ] }";
        JsonNode dataNode = new ObjectMapper().readTree(json).get("data");

        when(matchRepository.existsByMatchApiId("match-1")).thenReturn(false);
        when(matchRepository.existsByMatchApiId("match-2")).thenReturn(true); // 필터링됨

        // when
        Map<Member, List<Match>> result = memberServiceTestable.setMemberMatchMap(dataNode);

        // then
        assertThat(result).containsKey(member);
        assertThat(result.get(member)).hasSize(1);
        assertThat(result.get(member).get(0).getMatchApiId()).isEqualTo("match-1");
        assertThat(result.get(member).get(0).getGameMode()).isEqualTo(GameMode.NOTFOUND);
    }

    // MemberService에서 protected로 임시로 접근하게 래핑된 내부 클래스 (테스트 편의 목적)
    @InjectMocks
    private MemberServiceTestable memberServiceTestable;

    public static class MemberServiceTestable extends MemberService {
        public MemberServiceTestable(MemberRepository memberRepository,
                                     MatchRepository matchRepository,
                                     MemberMatchRepository memberMatchRepository,
                                     PubgApiRequestService pubgApiRequestService) {
            super(memberRepository, matchRepository, memberMatchRepository, pubgApiRequestService);
        }

        @Override
        public boolean checkFetchable(JsonNode dataNode) {
            return super.checkFetchable(dataNode);
        }

        @Override
        public Map<Member, List<Match>> setMemberMatchMap(JsonNode dataNode) {
            return super.setMemberMatchMap(dataNode);
        }
    }
}
