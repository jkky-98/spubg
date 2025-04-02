package com.jkky98.spubg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.pubg.request.PubgApiRequestService;
import com.jkky98.spubg.repository.MatchRepository;
import com.jkky98.spubg.repository.MemberMatchRepository;
import com.jkky98.spubg.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private PubgApiRequestService pubgApiRequestService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberMatchRepository memberMatchRepository;

    @InjectMocks
    private MatchService matchService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveAll() {
        List<Match> matches = List.of(Match.builder().build());
        when(matchRepository.saveAll(matches)).thenReturn(matches);
        List<Match> result = matchService.saveAll(matches);
        assertThat(result).isEqualTo(matches);
    }

    @Test
    void testSave() {
        Match match = Match.builder().build();
        when(matchRepository.save(match)).thenReturn(match);
        Match result = matchService.save(match);
        assertThat(result).isEqualTo(match);
    }

    @Test
    void testProcessMatchValid() throws Exception {
        Match match = Match.builder().build();
        match.setId(1L);
        match.setMatchApiId("match123");

        String mockJson = new String(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResourceAsStream("mock/match_response_valid.json")).readAllBytes());

        JsonNode rootNode = objectMapper.readTree(mockJson);

        when(pubgApiRequestService.requestMatch("match123")).thenReturn(rootNode);
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(memberRepository.findAll()).thenReturn(Collections.emptyList());
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

        matchService.processMatch(match);

        verify(matchRepository, atLeastOnce()).findById(1L);
    }

    @Test
    void testProcessMatchCustomMatch() throws Exception {
        Match match = Match.builder().build();
        match.setId(1L);
        match.setMatchApiId("customMatch");

        String mockJson = new String(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResourceAsStream("mock/match_response_custom.json")).readAllBytes());

        JsonNode rootNode = objectMapper.readTree(mockJson);

        when(pubgApiRequestService.requestMatch("customMatch")).thenReturn(rootNode);
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        matchService.processMatch(match);

        verify(matchRepository, atLeastOnce()).findById(1L);
        verify(matchRepository, never()).save(any());
    }

    @Test
    void testProcessMatchNonSquadMode() throws Exception {
        Match match = Match.builder().build();
        match.setId(1L);
        match.setMatchApiId("nonSquadMatch");

        String mockJson = new String(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResourceAsStream("mock/match_response_nonsquad.json")).readAllBytes());

        JsonNode rootNode = objectMapper.readTree(mockJson);

        when(pubgApiRequestService.requestMatch("nonSquadMatch")).thenReturn(rootNode);
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        matchService.processMatch(match);

        verify(matchRepository, atLeastOnce()).findById(1L);
        verify(matchRepository, never()).save(any());
    }

    // More edge cases can be added as needed...
}

