package com.jkky98.spubg.service.implement;

import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.Member;
import com.jkky98.spubg.domain.MemberMatch;
import com.jkky98.spubg.pubg.enums.GameMode;
import com.jkky98.spubg.repository.MatchRepository;
import com.jkky98.spubg.repository.MemberMatchRepository;
import com.jkky98.spubg.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MemberMatchReaderTest {

    @Autowired
    MemberMatchReader memberMatchReader;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MatchRepository matchRepository;

    @Autowired
    MemberMatchRepository memberMatchRepository;

    @Test
    @DisplayName("[MemberMatchReader][findByMemberAndMatch] 성공 테스트")
    void testFindByMemberAndMatch() {
        // given
        Member member = memberRepository.save(Member.builder().username("test-user").build());
        Match match = matchRepository.save(Match.builder().assetUrl("asset123").build());
        MemberMatch saved = memberMatchRepository.save(MemberMatch.builder()
                .member(member)
                .match(match)
                .build());

        // when
        Optional<MemberMatch> result = memberMatchReader.findByMemberAndMatch(member, match);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("[MemberMatchReader][read] 성공 테스트")
    void testRead() {
        // given
        MemberMatch saved = memberMatchRepository.save(MemberMatch.builder().build());

        // when
        MemberMatch result = memberMatchReader.read(saved.getId());

        // then
        assertThat(result.getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("[MemberMatchReader][getMemberMatchNeedToAnaysis] 분석 대상 조회 테스트")
    void testGetMemberMatchNeedToAnaysis() {
        // given
        Match match = matchRepository.save(Match.builder()
                .boolIsAnalysis(true)
                .gameMode(GameMode.SQUAD)
                .build());

        MemberMatch saved = memberMatchRepository.save(MemberMatch.builder()
                .match(match)
                .boolIsAnalysis(false)
                .build());

        // when
        List<MemberMatch> result = memberMatchReader.getMemberMatchNeedToAnaysis();

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getMatch().isBoolIsAnalysis()).isTrue();
        assertThat(result.get(0).isBoolIsAnalysis()).isFalse();
    }

    @Test
    @DisplayName("[MemberMatchReader][readAssetUrl] 자산 URL 조회 테스트")
    void testReadAssetUrl() {
        // given
        Match match = matchRepository.save(Match.builder().assetUrl("http://asset.test/pubg123").build());
        MemberMatch memberMatch = memberMatchRepository.save(MemberMatch.builder()
                .match(match)
                .build());

        // when
        String result = memberMatchReader.readAssetUrl(memberMatch.getId());

        // then
        assertThat(result).isEqualTo("http://asset.test/pubg123");
    }

    @Test
    @DisplayName("[MemberMatchReader][readAssetUrl] 예외 발생 테스트")
    void testReadAssetUrlException() {
        // when & then
        assertThatThrownBy(() -> memberMatchReader.readAssetUrl(9999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
