package com.jkky98.spubg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jkky98.spubg.domain.GameMode;
import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.Member;
import com.jkky98.spubg.domain.MemberMatch;
import com.jkky98.spubg.domain.init.InitMemberList;
import com.jkky98.spubg.pubg.request.PubgApiManager;
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
    private final PubgApiManager pubgApiManager;
    private final MemberMatchRepository memberMatchRepository;

    @Transactional
    public void fetchMember() {

        log.info("🔍 fetchMember() 시작");

        List<String> allUsernames = memberRepository.findAll().stream()
                .map(Member::getUsername)
                .toList();
        log.info("👥 총 {}명의 멤버 데이터 조회 완료", allUsernames.size());

        JsonNode rootNode = pubgApiManager.requestManyMember(allUsernames);
        JsonNode dataNode = rootNode.get("data");

        if (checkFetchable(dataNode)) {
            log.info("✅✅✅ 모든 매치가 이미 존재함. fetchMember() 종료 ✅✅✅");
            return;
        }

        Map<Member, List<Match>> memberMatchMap = setMemberMatchMap(dataNode);//memberMatchMap에 데이터 쌓기
        log.info("📦 memberMatchMap 데이터 생성 완료. 총 {}명의 멤버가 포함됨", memberMatchMap.size());

        List<Match> uniqueBulkMatch = getUniqueBulkMatch(memberMatchMap); // 유니크 매치 배열 만들기
        log.info("🔄 유니크한 매치 개수: {}", uniqueBulkMatch.size());

        List<Match> matchesSaved = matchRepository.saveAll(uniqueBulkMatch); // 유니크 매치 배열 리포지토리 저장
        log.info("💾 매치 데이터 저장 완료. 저장된 매치 개수: {}", matchesSaved.size());

        Map<String, Match> MatchMapSaved = matchesSaved.stream()
                .collect(Collectors.toMap(Match::getMatchApiId, match -> match));

        List<MemberMatch> bulkMemberMatch = new ArrayList<>();

        log.info("🔄 MemberMatch 매핑 시작");
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
        log.info("🔄 MemberMatch 매핑 완료. 총 {}개 매핑됨", bulkMemberMatch.size());

        memberMatchRepository.saveAll(bulkMemberMatch);
        log.info("💾 MemberMatch 저장 완료");
        log.info("✅ fetchMember() 완료");
    }

    private boolean checkFetchable(JsonNode dataNode) {
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
        Set<Match> bulkMatch = new HashSet<>();
        memberMatchMap.values().forEach(bulkMatch::addAll);
        return new ArrayList<>(bulkMatch);
    }

    private Map<Member, List<Match>> setMemberMatchMap(JsonNode dataNode) {
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
