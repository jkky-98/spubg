package com.jkky98.spubg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jkky98.spubg.domain.GameMode;
import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.Member;
import com.jkky98.spubg.domain.MemberMatch;
import com.jkky98.spubg.pubg.request.PubgApiRequestService;
import com.jkky98.spubg.repository.MatchRepository;
import com.jkky98.spubg.repository.MemberMatchRepository;
import com.jkky98.spubg.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final MatchRepository matchRepository;
    private final MemberMatchRepository memberMatchRepository;
    private final PubgApiRequestService pubgApiRequestService;

    @Transactional
    public void fetchMember() {

        List<String> allUsernames = memberRepository.findAll().stream()
                .map(Member::getUsername)
                .toList();

        JsonNode rootNode = pubgApiRequestService.requestManyMembers(allUsernames);
        JsonNode dataNode = rootNode.get("data");

        if (checkFetchable(dataNode)) {
            return;
        }

        Map<Member, List<Match>> memberMatchMap = setMemberMatchMap(dataNode);//memberMatchMap에 데이터 쌓기

        List<Match> uniqueBulkMatch = getUniqueBulkMatch(memberMatchMap); // 유니크 매치 배열 만들기

        List<Match> matchesSaved = matchRepository.saveAll(uniqueBulkMatch); // 유니크 매치 배열 리포지토리 저장

        Map<String, Match> MatchMapSaved = matchesSaved.stream()
                .collect(Collectors.toMap(Match::getMatchApiId, match -> match));

        List<MemberMatch> bulkMemberMatch = new ArrayList<>();

        memberMatchMap.forEach((member, matches) -> {
            matches.forEach(
                    match -> {
                        Match matchSaved = MatchMapSaved.get(match.getMatchApiId());
                        MemberMatch mm = MemberMatch.builder()
                                .member(member)
                                .match(matchSaved)
                                .boolIsAnalysis(false)
                                .build();

                        bulkMemberMatch.add(mm);
                    }
            );
        });

        memberMatchRepository.saveAll(bulkMemberMatch);
    }

    protected boolean checkFetchable(JsonNode dataNode) {
        Set<String> matchIds = new HashSet<>();

        dataNode.forEach(
                playerNode -> {
                    JsonNode matchesNode = playerNode.get("relationships").get("matches").get("data");
                    matchesNode.forEach(
                            matchNode -> {
                                matchIds.add(matchNode.get("id").asText());
                            }
                    );
                }
        );

        long count = matchRepository.countByMatchApiIdIn(new ArrayList<>(matchIds));
        return count == matchIds.size();
    }

    private static List<Match> getUniqueBulkMatch(Map<Member, List<Match>> memberMatchMap) {
        Map<String, Match> uniqueMatches = new HashMap<>();
        memberMatchMap.values().forEach(matchList ->
                matchList.forEach(match ->
                        uniqueMatches.putIfAbsent(match.getMatchApiId(), match)
                )
        );
        return new ArrayList<>(uniqueMatches.values());
    }


    protected Map<Member, List<Match>> setMemberMatchMap(JsonNode dataNode) {
        Map<Member, List<Match>> memberMatchMap = new HashMap<>();

        for (JsonNode playerNode : dataNode) {
            String accountId = playerNode.get("id").asText();
            Member member = memberRepository.findByAccountId(accountId).orElseThrow();

            for (JsonNode matchNode : playerNode.get("relationships").get("matches").get("data")) {
                String matchApiId = matchNode.get("id").asText();
                if (!matchRepository.existsByMatchApiId(matchApiId)) {
                    Match match = Match.builder()
                            .matchApiId(matchApiId)
                            .boolIsAnalysis(false)
                            .gameMode(GameMode.NOTFOUND)
                            .build();

                    if (!memberMatchMap.containsKey(member)) {
                        memberMatchMap.put(member, new ArrayList<>());
                        memberMatchMap.get(member).add(match);
                    } else {
                        memberMatchMap.get(member).add(match);
                    }
                }
            }
        }

        return memberMatchMap;
    }
}
