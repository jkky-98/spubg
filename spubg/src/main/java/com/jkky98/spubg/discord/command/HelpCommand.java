package com.jkky98.spubg.discord.command;

import com.jkky98.spubg.discord.Command;
import com.jkky98.spubg.discord.service.MessageResponseService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HelpCommand implements Command {

    private final MessageResponseService messageResponseService;

    @Override
    public String getCommandName() {
        return "도움";
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        messageResponseService.sendHelp(event);
    }
}
