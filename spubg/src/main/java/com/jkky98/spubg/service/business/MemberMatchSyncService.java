package com.jkky98.spubg.service.business;

import com.fasterxml.jackson.databind.JsonNode;
import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.Member;
import com.jkky98.spubg.domain.MemberMatch;
import com.jkky98.spubg.service.implement.MemberMatchReader;
import com.jkky98.spubg.service.implement.MemberMatchWriter;
import com.jkky98.spubg.service.implement.MemberReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberMatchSyncService {

    private final MemberReader memberReader;
    private final MemberMatchReader memberMatchReader;
    private final MemberMatchWriter memberMatchWriter;

    @Transactional
    public void syncMemberMatchIfMissing(JsonNode rootNode, Match matchRead) {
        List<Member> membersRegistered = memberReader.readAll();
        List<String> membersAccountIdsRegistered = membersRegistered
                .stream()
                .map(Member::getAccountId)
                .toList();

        JsonNode includedArrayNode = rootNode.path("included");

        for (JsonNode includedNode : includedArrayNode) {
            if (includedNode.path("type").asText().equals("participant")) {
                String accountId = includedNode.path("attributes").path("stats").get("playerId").asText();

                if (membersAccountIdsRegistered.contains(accountId)) {
                    membersRegistered.stream()
                            .filter(m -> m.getAccountId().equals(accountId))
                            .findFirst()
                            .ifPresent(m -> {
                                Optional<MemberMatch> memberAndMatchOptinal = memberMatchReader.findByMemberAndMatch(m, matchRead);

                                memberAndMatchOptinal.orElseGet(
                                        () -> {
                                            MemberMatch memberMatchNew = MemberMatch.builder()
                                                    .member(m)
                                                    .match(matchRead)
                                                    .boolIsAnalysis(false)
                                                    .build();

                                            MemberMatch savedNewMemberMatch = memberMatchWriter.save(memberMatchNew);
                                            log.info("매치 ID : {}에 누락된 Member가 존재합니다. MemberMatch를 추가합니다. : {}", matchRead.getId(), savedNewMemberMatch.getId());
                                            return savedNewMemberMatch;
                                        });
                            });

                }
            }
        }
    }
}
