package com.jkky98.spubg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jkky98.spubg.domain.GameMode;
import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.Member;
import com.jkky98.spubg.domain.MemberMatch;
import com.jkky98.spubg.domain.init.InitMemberList;
import com.jkky98.spubg.pubg.request.PubgApiManager;
import com.jkky98.spubg.repository.MemberMatchRepository;
import com.jkky98.spubg.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final PubgApiManager pubgApiManager;
    private final MemberRepository memberRepository;
    private final MemberMatchRepository memberMatchRepository;
    private final MatchService matchService;

    @Transactional
    public void initMember() {
        List<String> initMembers = InitMemberList.list;

        JsonNode jsonNode = pubgApiManager.requestManyMember(initMembers);

        List<Member> initMembersSaved = new ArrayList<>();
        Map<Member, List<Match>> initMatchAnalysisMap = new HashMap<>();
        List<MemberMatch> memberMatchList = new ArrayList<>();

        JsonNode dataNode = jsonNode.get("data");

        setMembersAndMatches(initMembers, dataNode, initMembersSaved, initMatchAnalysisMap);

        List<Member> membersSaved = memberRepository.saveAll(initMembersSaved);

        List<Match> uniqueMatches = initMatchAnalysisMap.values().stream()
                .flatMap(List::stream)  // `List<Match>` → 개별 `Match`로 변환
                .collect(Collectors.toMap(
                        Match::getMatchApiId,
                        match -> match,
                        (existing, replacement) -> existing // 중복된 경우 기존 값 유지
                ))
                .values()
                .stream()
                .toList();

        System.out.println("SIZE: " + uniqueMatches.size());

        List<Match> matchesSaved = matchService.saveAll(uniqueMatches);

        ofMemberMatchs(initMatchAnalysisMap, memberMatchList, membersSaved, matchesSaved);

        memberMatchRepository.saveAll(memberMatchList);
    }

    private static void ofMemberMatchs(
            Map<Member, List<Match>> initMatchAnalysisMap,
            List<MemberMatch> memberMatchList,
            List<Member> membersSaved,
            List<Match> matchesSaved
    ) {
        // 저장된 Member와 Match를 매핑하여 빠르게 조회할 수 있도록 Map 생성
        Map<String, Member> savedMemberMap = membersSaved.stream()
                .collect(Collectors.toMap(Member::getUsername, member -> member));

        Map<String, Match> savedMatchMap = matchesSaved.stream()
                .collect(Collectors.toMap(Match::getMatchApiId, match -> match));

        // 기존 initMatchAnalysisMap을 순회하면서, 저장된 Member 및 Match로 MemberMatch 생성
        initMatchAnalysisMap.forEach((member, matches) -> {
            Member savedMember = savedMemberMap.get(member.getUsername()); // 저장된 Member 가져오기

            for (Match match : matches) {
                Match savedMatch = savedMatchMap.get(match.getMatchApiId());

                MemberMatch mm = MemberMatch.builder()
                        .member(savedMember)
                        .match(savedMatch)
                        .boolIsAnalysis(false)
                        .build();

                memberMatchList.add(mm);
            }
        });
    }


    /**
     * 응답 JsonNode에서 Member 객체들 구성, Match 객체들 구성
     * @param initMembers
     * @param dataNode
     * @param initMembersSaved
     * @param initMatchAnalysisMap
     */
    private static void setMembersAndMatches(List<String> initMembers, JsonNode dataNode, List<Member> initMembersSaved, Map<Member, List<Match>> initMatchAnalysisMap) {
        for (int i = 0; i < initMembers.size(); i++) {
            JsonNode memberNode = dataNode.get(i);

            JsonNode memberAttributesNode = memberNode.get("attributes");

            Member memberSaved = Member.builder()
                    .accountId(memberNode.get("id").asText())
                    .clanId(memberAttributesNode.get("clanId").asText())
                    .banType(memberAttributesNode.get("banType").asText())
                    .username(memberAttributesNode.get("name").asText())
                    .build();

            initMembersSaved.add(memberSaved);

            JsonNode matchesNode = memberNode.get("relationships").get("matches").get("data");

            System.out.println("매치노드 크기 : " + matchesNode.size());

            setInitMatchAnalysisListSaved(matchesNode, initMatchAnalysisMap, memberSaved);
        }
    }

    private static void setInitMatchAnalysisListSaved(JsonNode matchesNode, Map<Member, List<Match>> initMatchAnalysisMap, Member member) {
        if (matchesNode.isArray()) {
            for (JsonNode matchNode : matchesNode) {
                String matchId = matchNode.get("id").asText();
                System.out.println(member.getUsername() + ":: matchId : " + matchId);

                Match match = Match.builder()
                        .matchApiId(matchId)
                        .boolIsAnalysis(false)
                        .gameMode(GameMode.NOTFOUND)
                        .build();
                if (!initMatchAnalysisMap.containsKey(member)) {
                    initMatchAnalysisMap.put(member, new ArrayList<>());
                    initMatchAnalysisMap.get(member).add(match);
                } else {
                    initMatchAnalysisMap.get(member).add(match);
                }
            }
        }
    }
}
