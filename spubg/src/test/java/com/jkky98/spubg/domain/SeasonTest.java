package com.jkky98.spubg.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SeasonTest {

    @Test
    @DisplayName("[Season][updateBoolIsCurrentSeason] 단위 테스트")
    void updateBoolIsCurrentSeason() {
        // given
        Season season = Season.builder()
                .seasonApiId("2024-SUMMER")
                .boolIsCurrentSeason(true)  // 초기 상태
                .boolIsOffseason(false)
                .build();

        // when
        season.updateBoolIsCurrentSeason();

        // then
        assertThat(season.isBoolIsCurrentSeason()).isFalse();

        // when (한 번 더 호출)
        season.updateBoolIsCurrentSeason();

        // then
        assertThat(season.isBoolIsCurrentSeason()).isTrue();
    }
}
