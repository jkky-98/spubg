package com.jkky98.spubg.service.implement;

import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.Member;
import com.jkky98.spubg.domain.MemberMatch;
import com.jkky98.spubg.pubg.enums.GameMode;
import com.jkky98.spubg.repository.MatchRepository;
import com.jkky98.spubg.repository.MemberMatchRepository;
import com.jkky98.spubg.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MemberMatchWriterTest {

    @Autowired
    MemberMatchWriter memberMatchWriter;

    @Autowired
    MatchRepository matchRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("[MemberMatchWriter][save] 저장 성공 테스트")
    void testSave() {
        // given
        Member member = memberRepository.save(Member.builder().username("test-user").build());
        Match match = matchRepository.save(Match.builder().gameMode(GameMode.SQUAD).build());

        MemberMatch memberMatch = MemberMatch.builder()
                .member(member)
                .match(match)
                .boolIsAnalysis(false)
                .build();

        // when
        MemberMatch saved = memberMatchWriter.save(memberMatch);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getMember().getId()).isEqualTo(member.getId());
        assertThat(saved.getMatch().getId()).isEqualTo(match.getId());
        assertThat(saved.isBoolIsAnalysis()).isFalse();
    }
}
