package com.jkky98.spubg.discord.command;

import com.jkky98.spubg.discord.Command;
import com.jkky98.spubg.discord.domain.Member;
import com.jkky98.spubg.discord.repository.MemberMapper;
import com.jkky98.spubg.discord.service.MessageResponseService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberCommand implements Command {

    private final MemberMapper mapper;
    private final MessageResponseService messageResponseService;

    @Override
    public String getCommandName() {
        return "멤버";
    }

    @Override
    @Transactional(readOnly = true)
    public void execute(MessageReceivedEvent event, String[] args) {
        List<Member> members = mapper.findAll();
        messageResponseService.sendMembers(event, members);
    }
}
