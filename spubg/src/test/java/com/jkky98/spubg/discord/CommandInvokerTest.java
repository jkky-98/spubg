package com.jkky98.spubg.discord;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;

import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommandInvokerUnitTest {

    private CommandInvoker invoker;
    private MessageReceivedEvent event;
    private MessageChannelUnion channel;
    private MessageCreateAction action;

    @BeforeEach
    void setUp() {
        // 1) 등록된 커맨드 없는 상태로 인보커 초기화
        invoker = new CommandInvoker(List.of());

        // 2) event, channel, action 목 객체 준비
        event   = mock(MessageReceivedEvent.class);
        channel = mock(MessageChannelUnion.class);
        action  = mock(MessageCreateAction.class);

        // 3) unknown 커맨드일 때 에러 메시지 전송 플로우 stub
        when(event.getChannel()).thenReturn(channel);
        when(channel.sendMessage(
                eq("❌ 알 수 없는 명령어입니다. `!help`를 통해 사용법을 알아보세요!")
        )).thenReturn(action);
    }

    @Test
    @DisplayName("[CommandInvoker][handleCommand] 미등록 커맨드면 에러 메시지 전송 후 queue() 호출")
    void handleCommand_unknown_sendsErrorAndQueues() {
        // when
        invoker.handleCommand("noSuchCmd", event, new String[]{"foo"});

        // then
        verify(channel).sendMessage(
                "❌ 알 수 없는 명령어입니다. `!help`를 통해 사용법을 알아보세요!"
        );
        verify(action).queue();
    }

    @Test
    @DisplayName("[CommandInvoker][handleCommand] 등록된 커맨드면 execute()만 호출되고, 메시지 전송은 절대 일어나지 않는다")
    void handleCommand_known_executesCommandOnly() {
        // when
        Command mockCmd = mock(Command.class);
        when(mockCmd.getCommandName()).thenReturn("cmd");
        invoker = new CommandInvoker(List.of(mockCmd));

        String[] args = {"a", "b"};
        invoker.handleCommand("cmd", event, args);

        // then
        verify(mockCmd).execute(event, args);
        // 에러 메시지 플로우는 타지 않아야 함
        verify(channel, never()).sendMessage(anyString());
        verify(action, never()).queue();
    }
}
