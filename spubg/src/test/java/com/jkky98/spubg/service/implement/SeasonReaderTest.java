package com.jkky98.spubg.service.implement;

import com.jkky98.spubg.domain.Season;
import com.jkky98.spubg.repository.SeasonRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SeasonReaderTest {

    @Autowired
    SeasonReader seasonReader;

    @Autowired
    SeasonRepository seasonRepository;

    @Test
    @DisplayName("[SeasonReader][readCurrentSeason] 현재 시즌 조회 성공 테스트")
    void testReadCurrentSeason() {
        // given
        Season current = Season.builder()
                .seasonApiId("seasonApiId")
                .boolIsCurrentSeason(true)
                .build();
        seasonRepository.save(current);

        // when
        Season result = seasonReader.readCurrentSeason();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSeasonApiId()).isEqualTo("seasonApiId");
        assertThat(result.isBoolIsCurrentSeason()).isTrue();
    }

    @Test
    @DisplayName("[SeasonReader][readCurrentSeason] 현재 시즌이 없을 경우 예외 테스트")
    void testReadCurrentSeasonNotFound() {
        // given
        seasonRepository.save(Season.builder()
                .seasonApiId("lastSeasonApiId")
                .boolIsCurrentSeason(false)
                .build());

        // when & then
        assertThatThrownBy(() -> seasonReader.readCurrentSeason())
                .isInstanceOf(EntityNotFoundException.class);
    }
}

