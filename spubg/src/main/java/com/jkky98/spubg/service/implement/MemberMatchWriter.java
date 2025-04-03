package com.jkky98.spubg.service.implement;

import com.jkky98.spubg.domain.MemberMatch;
import com.jkky98.spubg.repository.MemberMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class MemberMatchWriter {
    private final MemberMatchRepository memberMatchRepository;

    public MemberMatch save(MemberMatch memberMatch) {
        return memberMatchRepository.save(memberMatch);
    }
}
