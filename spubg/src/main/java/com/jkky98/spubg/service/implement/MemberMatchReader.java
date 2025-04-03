package com.jkky98.spubg.service.implement;

import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.Member;
import com.jkky98.spubg.domain.MemberMatch;
import com.jkky98.spubg.repository.MemberMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberMatchReader {

    private final MemberMatchRepository memberMatchRepository;

    public Optional<MemberMatch> findByMemberAndMatch(Member member, Match match) {
        return memberMatchRepository.findByMemberAndMatch(member, match);
    }
}
