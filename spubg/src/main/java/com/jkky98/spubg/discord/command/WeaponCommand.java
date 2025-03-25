package com.jkky98.spubg.discord.command;

import com.jkky98.spubg.discord.Command;
import com.jkky98.spubg.discord.domain.Member;
import com.jkky98.spubg.discord.domain.WeaponRanking;
import com.jkky98.spubg.discord.repository.MemberMapper;
import com.jkky98.spubg.discord.repository.StaticMapper;
import com.jkky98.spubg.discord.service.MessageResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeaponCommand implements Command {

    private final StaticMapper staticMapper;
    private final MemberMapper memberMapper;
    private final MessageResponseService messageResponseService;

    @Override
    public String getCommandName() {
        return "웨폰마스터";
    }

    @Override
    @Transactional
    public void execute(MessageReceivedEvent event, String[] args) {
        List<WeaponRanking> topWeaponRankings = staticMapper.getTopWeaponRankings();
        messageResponseService.sendWeaponRankingTable(event, topWeaponRankings);
    }
}
