package com.jkky98.spubg.service.implement;

import com.jkky98.spubg.domain.Member;
import com.jkky98.spubg.repository.MemberRepository;
import io.netty.handler.codec.EncoderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberReader {

    private final MemberRepository memberRepository;

    public Member read(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(EncoderException::new);
    }

    public List<Member> readAll() {
        return memberRepository.findAll();
    }
}
