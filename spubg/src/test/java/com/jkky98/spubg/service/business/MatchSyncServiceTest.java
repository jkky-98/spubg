package com.jkky98.spubg.service.business;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkky98.spubg.domain.GameMode;
import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.Season;
import com.jkky98.spubg.service.implement.MatchReader;
import com.jkky98.spubg.service.implement.SeasonReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MatchSyncServiceTest {

    @Mock
    private MatchReader matchReader;

    @Mock
    private MemberMatchSyncService memberMatchSyncService;

    @Mock
    private SeasonReader seasonReader;

    @InjectMocks
    private MatchSyncService matchSyncService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("[MatchSyncService][sync] 성공 테스트")
    void syncSuccess() throws IOException {
        // given
        InputStream resourceAsStream = getClass().getResourceAsStream("/mock/match_response.json");
        JsonNode rootNode = objectMapper.readTree(resourceAsStream);

        Match matchRead = Match.builder()
                .id(1L)
                .matchApiId("testMatchApiId")
                .boolIsAnalysis(false)
                .gameMode(GameMode.NOTFOUND)
                .build();

        Season season = Season.builder()
                .id(1L)
                .boolIsCurrentSeason(true)
                .seasonApiId("currentSeasonApiId")
                .build();

        Long matchId = 1L;

        // when
        when(matchReader.read(matchId)).thenReturn(matchRead);
        doNothing().when(memberMatchSyncService).syncMemberMatchIfMissing(any(JsonNode.class), eq(matchRead));
        when(seasonReader.readCurrentSeason()).thenReturn(season);

        matchSyncService.sync(matchId, rootNode);

        // then
        assertThat(matchRead.isBoolIsAnalysis()).isTrue(); // 분석 완료 검증
        assertThat(matchRead.getGameMode()).isEqualTo(GameMode.SQUAD); // 게임 모드 설정 검증
        assertThat(matchRead.getSeason()).isEqualTo(season.getSeasonApiId()); // 시즌 값 설정 검증
        assertThat(matchRead.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 4, 8, 21, 0, 0)); // 매치 시간 검증 (Asia/Seoul -> + 9hours)
        assertThat(matchRead.getAssetId()).isEqualTo("asset123");
        assertThat(matchRead.getAssetUrl()).isEqualTo("https://example.com/assets/asset123.jpg");
        assertThat(matchRead.getMap()).isEqualTo("Erangel");
    }

    @Test
    @DisplayName("[MatchSyncService][sync] validMatchSync 필터링 테스트 - 에러 결과를 담은 json이 반환될 경우")
    void syncFailErrorNode() throws IOException {
        //given
        failTestLogic("/mock/match_response_not_found.json");
    }

    @Test
    @DisplayName("[MatchSyncService][sync] validMatchSync 필터링 테스트 - dataNode 없을경우")
    void syncFailDataNode() throws IOException {
        //given
        failTestLogic("/mock/match_response_no_datanode.json");
    }

    @Test
    @DisplayName("[MatchSyncService][sync] validMatchSync 필터링 테스트 - 올바른 type이 아닐 경우")
    void syncFailTypeFail() throws IOException {
        failTestLogic("/mock/match_response_type_fail.json");
    }

    @Test
    @DisplayName("[MatchSyncService][sync] validMatchSync 필터링 테스트 - attributes 노드 존재하지 않을 경우")
    void syncFailAttributesFail() throws IOException {
        failTestLogic("/mock/match_response_attributes_fail.json");
    }

    @Test
    @DisplayName("[MatchSyncService][sync] validMatchSync 필터링 테스트 - 커스텀 매치일 경우")
    void syncFailIsCustomMatch() throws IOException {
        failTestLogic("/mock/match_response_custom_match.json");
    }

    @Test
    @DisplayName("[MatchSyncService][sync] validMatchSync 필터링 테스트 - 오피셜 타입 매치가 아닐 경우")
    void syncFailNotOfficial() throws IOException {
        failTestLogic("/mock/match_response_not_official.json");
    }

    @Test
    @DisplayName("[MatchSyncService][sync] validMatchSync 필터링 테스트 - 스쿼드 게임모드가 아닐 경우")
    void syncFailNotSquad() throws IOException {
        failTestLogic("/mock/match_response_not_squad.json");
    }

    @Test
    @DisplayName("[MatchSyncService][sync] validMatchSync 필터링 테스트 - included노드가 존재하지 않을 경우")
    void syncFailNoIncludedNode() throws IOException {
        failTestLogic("/mock/match_response_no_included.json");
    }

    @Test
    @DisplayName("[MatchSyncService][sync] validMatchSync 필터링 테스트 - mapName 노드가 존재하지 않을 경우")
    void syncFailNoMapName() throws IOException {
        failTestLogicAfterValid("/mock/match_response_no_mapName.json");
    }

    @Test
    @DisplayName("[MatchSyncService][sync] validMatchSync 필터링 테스트 - createAt 노드가 존재하지 않을 경우")
    void syncFailNoCreated() throws IOException {
        failTestLogicAfterValid("/mock/match_response_no_created.json");
    }

    @Test
    @DisplayName("[MatchSyncService][sync] validMatchSync 필터링 테스트 - createAt 형식이 이상할 경우")
    void syncFailCreatedSyntexException() throws IOException {
        failTestLogicAfterValid("/mock/match_response_wrong_created.json");
    }

    @Test
    @DisplayName("[MatchSyncService][sync] validMatchSync 필터링 테스트 - AssetId가 존재하지 않을 경우")
    void syncFailNoAssetId() throws IOException {
        failTestLogicAfterValid("/mock/match_response_no_assetId.json");
    }

    @Test
    @DisplayName("[MatchSyncService][sync] validMatchSync 필터링 테스트 - AssetURL가 존재하지 않을 경우")
    void syncFailNoAssetURL() throws IOException {
        failTestLogicAfterValid("/mock/match_response_no_assetUrl.json");
    }

    private void failTestLogicAfterValid(String testJsonPath) throws IOException {
        //given
        InputStream resourceAsStream = getClass().getResourceAsStream(testJsonPath);
        JsonNode rootNode = objectMapper.readTree(resourceAsStream);

        Match matchRead = Match.builder()
                .id(1L)
                .matchApiId("testMatchApiId")
                .boolIsAnalysis(false)
                .gameMode(GameMode.NOTFOUND)
                .build();

        Season season = Season.builder()
                .id(1L)
                .boolIsCurrentSeason(true)
                .seasonApiId("currentSeasonApiId")
                .build();

        Long matchId = 1L;

        // when
        when(matchReader.read(matchId)).thenReturn(matchRead);
        doNothing().when(memberMatchSyncService).syncMemberMatchIfMissing(any(JsonNode.class), eq(matchRead));
        when(seasonReader.readCurrentSeason()).thenReturn(season);

        matchSyncService.sync(matchId, rootNode);

        //then
        assertThat(matchRead.isBoolIsAnalysis()).isTrue();
        assertThat(matchRead.getGameMode()).isEqualTo(GameMode.OTHER);
    }

    private void failTestLogic(String testJsonPath) throws IOException {
        //given
        InputStream resourceAsStream = getClass().getResourceAsStream(testJsonPath);
        JsonNode rootNode = objectMapper.readTree(resourceAsStream);

        Match matchRead = Match.builder()
                .build();

        Long matchId = 1L;

        //when
        when(matchReader.read(matchId)).thenReturn(matchRead);
        matchSyncService.sync(matchId, rootNode);
        //then
        assertThat(matchRead.isBoolIsAnalysis()).isTrue();
        assertThat(matchRead.getGameMode()).isEqualTo(GameMode.OTHER);
    }
}
