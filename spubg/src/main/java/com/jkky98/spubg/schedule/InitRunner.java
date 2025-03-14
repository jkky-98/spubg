package com.jkky98.spubg.schedule;

import com.jkky98.spubg.repository.MemberRepository;
import com.jkky98.spubg.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitRunner implements ApplicationRunner {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (memberRepository.findAll().isEmpty()) {
            memberService.initMember();
        }
    }
}
