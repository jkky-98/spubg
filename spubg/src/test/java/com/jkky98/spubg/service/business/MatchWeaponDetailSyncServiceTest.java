package com.jkky98.spubg.service.business;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.MatchWeaponDetail;
import com.jkky98.spubg.domain.Member;
import com.jkky98.spubg.domain.MemberMatch;
import com.jkky98.spubg.service.implement.MatchWeaponDetailWriter;
import com.jkky98.spubg.service.implement.MemberMatchReader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MatchWeaponDetailSyncServiceTest {

    @Mock
    private MemberMatchReader memberMatchReader;

    @Mock
    private MatchWeaponDetailWriter matchWeaponDetailWriter;

    @InjectMocks
    private MatchWeaponDetailSyncService matchWeaponDetailSyncService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("[MatchWeaponDetailSyncService][sync] sync 성공 테스트")
    void syncSuccess() throws IOException {
        // given
        InputStream resourceAsStream = getClass().getResourceAsStream("/matchweapondetail/match_weapon_detail_sync_test.json");
        JsonNode rootNode = objectMapper.readTree(resourceAsStream);

        MemberMatch memberMatch = MemberMatch.builder()
                .id(1L)
                .boolIsAnalysis(false)
                .match(Match.builder().id(1L).build())
                .member(Member.builder().id(1L).accountId("account123").build())
                .build();

        Long memberMatchId = 1L;

        // when
        when(memberMatchReader.read(memberMatchId)).thenReturn(memberMatch);
        when(matchWeaponDetailWriter.saveAll(anyList()))
                .thenReturn(List.of(MatchWeaponDetail.builder().build()));

        matchWeaponDetailSyncService.sync(memberMatchId, rootNode);

        // then
        assertThat(memberMatch.isBoolIsAnalysis()).isTrue();
    }

    @Test
    @DisplayName("[MatchWeaponDetailSyncService][sync] rootNode가 배열의 형태를 가지지 않을 경우 예외 발생")
    void syncRootNodeIsNotArrayNode() throws IOException {
        // given
        InputStream resourceAsStream = getClass().getResourceAsStream("/matchweapondetail/match_weapon_not_array.json");
        JsonNode rootNode = objectMapper.readTree(resourceAsStream);
        Long memberMatchId = 1L;
        // when
        // then
        Assertions.assertThatThrownBy(() -> matchWeaponDetailSyncService.sync(memberMatchId, rootNode)).isInstanceOf(RuntimeException.class);
    }
}
