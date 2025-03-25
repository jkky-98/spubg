package com.jkky98.spubg.discord.command;

import com.jkky98.spubg.discord.Command;
import com.jkky98.spubg.discord.domain.SmokeRanking;
import com.jkky98.spubg.discord.repository.StaticMapper;
import com.jkky98.spubg.discord.service.MessageResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmokeCommand implements Command {

    private final StaticMapper staticMapper;
    private final MessageResponseService messageResponseService;

    @Override
    public String getCommandName() {
        return "연막왕";
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        List<SmokeRanking> smokeRankings = staticMapper.getSmokeRankings();
        messageResponseService.sendSmokeRanking(event, smokeRankings);
    }
}
