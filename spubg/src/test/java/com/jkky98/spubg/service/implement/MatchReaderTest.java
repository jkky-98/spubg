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

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MatchReaderTest {

    @Autowired
    MatchReader matchReader;

    @Autowired
    MatchRepository matchRepository;

    @Test
    @DisplayName("[MatchReader][read] 성공 테스트")
    void testReadById() {
        Match saved = matchRepository.save(Match.builder().build());
        Match read = matchReader.read(saved.getId());

        assertThat(read.getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("[MatchReader][readByBoolIsAnalysisFalse] 성공 테스트")
    void testReadByBoolIsAnalysisFalse() {
        matchRepository.save(Match.builder().boolIsAnalysis(false).build()); // 분석 안 됨
        matchRepository.save(Match.builder().boolIsAnalysis(true).build());  // 분석 완료됨

        List<Match> result = matchReader.readByBoolIsAnalysisFalse();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isBoolIsAnalysis()).isFalse();
    }
}

