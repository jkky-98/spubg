package com.jkky98.spubg.discord;

import com.jkky98.spubg.discord.CommandInvoker;
import com.jkky98.spubg.discord.DiscordCommandListener;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DiscordCommandListenerTest {

    @Mock
    private CommandInvoker commandInvoker;

    @Mock
    private MessageReceivedEvent event;

    @Mock
    private User author;

    @Mock
    private Message message;

    @InjectMocks
    private DiscordCommandListener discordCommandListener;

    @Test
    @DisplayName("[DiscordCommandListener][onMessageReceived] 봇이 보낸 메세지 -> PASS")
    void onMessageReceived_bot_noInvocation() {
        // given
        when(event.getAuthor()).thenReturn(author);
        when(author.isBot()).thenReturn(true);

        // when
        discordCommandListener.onMessageReceived(event);

        // then
        verifyNoInteractions(commandInvoker);
    }

    @Test
    @DisplayName("[DiscordCommandListener][onMessageReceived] 명령어에 '!' 없을 경우 무시 ")
    void onMessageReceived_noPrefix_noInvocation() {
        // given
        when(event.getAuthor()).thenReturn(author);
        when(author.isBot()).thenReturn(false);
        when(event.getMessage()).thenReturn(message);
        when(message.getContentRaw()).thenReturn("테스트");
        // when
        discordCommandListener.onMessageReceived(event);
        // then
        verifyNoInteractions(commandInvoker);
    }

    @Test
    @DisplayName("[DiscordCommandListener][onMessageReceived] 명령어에 '!' 있을 경우 - 인자 X")
    void onMessageReceived_success_withoutArgs() {
        // given
        when(event.getAuthor()).thenReturn(author);
        when(author.isBot()).thenReturn(false);
        when(event.getMessage()).thenReturn(message);
        when(message.getContentRaw()).thenReturn("!cmd");
        // when
        discordCommandListener.onMessageReceived(event);
        // then
        ArgumentCaptor<String[]> cap = ArgumentCaptor.forClass(String[].class);
        verify(commandInvoker).handleCommand(eq("cmd"), eq(event), cap.capture());
        Assertions.assertThat(cap.getValue().length).isEqualTo(0);
    }

    @Test
    @DisplayName("[DiscordCommandListener][onMessageReceived] 명령어에 '!' 있을 경우 - 인자 O")
    void onMessageReceived_success_withArgs() {
        // given
        when(event.getAuthor()).thenReturn(author);
        when(author.isBot()).thenReturn(false);
        when(event.getMessage()).thenReturn(message);
        when(message.getContentRaw()).thenReturn("!cmd arg1 arg2");
        // when
        discordCommandListener.onMessageReceived(event);
        // then
        ArgumentCaptor<String[]> cap = ArgumentCaptor.forClass(String[].class);
        verify(commandInvoker).handleCommand(eq("cmd"), eq(event), cap.capture());
        Assertions.assertThat(cap.getValue().length).isEqualTo(2);
    }
}
