package com.jkky98.spubg.service.business;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.Member;
import com.jkky98.spubg.domain.MemberMatch;
import com.jkky98.spubg.service.implement.MemberMatchReader;
import com.jkky98.spubg.service.implement.MemberMatchWriter;
import com.jkky98.spubg.service.implement.MemberReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MemberMatchSyncServiceTest {
    @Mock
    private MemberReader memberReader;
    @Mock
    private MemberMatchReader memberMatchReader;
    @Mock
    private MemberMatchWriter memberMatchWriter;

    @InjectMocks
    private MemberMatchSyncService memberMatchSyncService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("[MemberMatchSyncService][syncMemberMatchIfMissing] 성공 테스트")
    void syncMemberMatchIfMissingSuccess() throws IOException {
        //given
        Member member1 = Member.builder()
                .accountId("account1")
                .build();

        Member member2 = Member.builder()
                .accountId("account2")
                .build();

        Match matchRead = Match.builder().id(1L).build();

        MemberMatch memberMatch1 = MemberMatch.builder().id(1L).member(member1).match(matchRead).build();

        InputStream resourceAsStream = getClass().getResourceAsStream("/membermatchsync/member_match_sync_test.json");
        JsonNode rootNode = objectMapper.readTree(resourceAsStream);
        //when
        when(memberReader.readAll()).thenReturn(List.of(member1, member2));
        when(memberMatchReader.findByMemberAndMatch(member1, matchRead)).thenReturn(Optional.empty());
        when(memberMatchWriter.save(any(MemberMatch.class))).thenReturn(memberMatch1);

        //then
        memberMatchSyncService.syncMemberMatchIfMissing(rootNode, matchRead);
    }
}
