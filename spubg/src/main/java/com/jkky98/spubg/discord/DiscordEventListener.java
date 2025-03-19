package com.jkky98.spubg.discord;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Slf4j
public class DiscordEventListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        log.info("[sender: {}] Received Message: {}",event.getAuthor().getName(), event.getMessage().getContentRaw());
        if (event.getAuthor().isBot()) return;
        event.getChannel().sendMessage("Hello, " + event.getAuthor().getName() + "!").queue();
    }
}
