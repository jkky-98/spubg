package com.jkky98.spubg.discord.command;

import com.jkky98.spubg.discord.Command;
import com.jkky98.spubg.discord.domain.LotOfFireRanking;
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
public class LotOfFireCommand implements Command {

    private final MessageResponseService messageResponseService;
    private final StaticMapper staticMapper;

    @Override
    public String getCommandName() {
        return "발사왕";
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        List<LotOfFireRanking> lotOfFireRankings = staticMapper.getLotOfFireRankings();
        messageResponseService.sendLotOfFireRanking(event, lotOfFireRankings);
    }
}
