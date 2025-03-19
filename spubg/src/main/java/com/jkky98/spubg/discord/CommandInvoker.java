package com.jkky98.spubg.discord;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommandInvoker {
    private final Map<String, Command> commands;

    @Autowired
    public CommandInvoker(List<Command> commandList) {
        this.commands = new HashMap<>();
        for (Command cmd : commandList) {
            this.commands.put(cmd.getCommandName().toLowerCase(), cmd);
        }
    }

    public void handleCommand(String command, MessageReceivedEvent event, String[] args) {
        Command cmd = commands.get(command.toLowerCase());
        if (cmd != null) {
            cmd.execute(event, args);
        } else {
            event.getChannel().sendMessage("❌ 알 수 없는 명령어입니다. `!help`를 통해 사용법을 알아보세요!").queue();
        }
    }
}
