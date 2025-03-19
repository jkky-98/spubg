package com.jkky98.spubg.discord;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface Command {
    String getCommandName();
    void execute(MessageReceivedEvent event, String[] args);
}
