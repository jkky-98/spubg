package com.jkky98.spubg.service.implement;

import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.repository.MatchRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MatchWriterTest {

    @Autowired
    MatchWriter matchWriter;

    @Autowired
    MatchRepository matchRepository;

    @Test
    @DisplayName("[MatchWriter][createAll] 저장 성공 테스트")
    void testCreateAll() {
        // given
        Match match1 = Match.builder().boolIsAnalysis(false).build();
        Match match2 = Match.builder().boolIsAnalysis(true).build();
        List<Match> inputList = List.of(match1, match2);

        // when
        List<Match> savedList = matchWriter.createAll(inputList);

        // then
        assertThat(savedList).hasSize(2);
        assertThat(savedList).allMatch(m -> m.getId() != null);
        assertThat(matchRepository.findAll()).hasSize(2);
    }
}
