package com.jkky98.spubg.service.schedule;

import com.jkky98.spubg.repository.MemberRepository;
import com.jkky98.spubg.service.InitService;
import com.jkky98.spubg.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitRunner implements ApplicationRunner {

    private final InitService initService;
    private final MemberRepository memberRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (memberRepository.findAll().isEmpty()) {
            initService.initMember();
        }
    }
}
