package com.jkky98.spubg.service.implement;

import com.jkky98.spubg.domain.Member;
import com.jkky98.spubg.repository.MemberRepository;
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
class MemberReaderTest {

    @Autowired
    MemberReader memberReader;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("[MemberReader][readAll] 전체 회원 조회 성공 테스트")
    void testReadAll() {
        // given
        Member member1 = memberRepository.save(Member.builder().username("유저1").build());
        Member member2 = memberRepository.save(Member.builder().username("유저2").build());

        // when
        List<Member> members = memberReader.readAll();

        // then
        assertThat(members).isNotEmpty();
        assertThat(members).extracting(Member::getUsername)
                .contains("유저1", "유저2");
    }
}
