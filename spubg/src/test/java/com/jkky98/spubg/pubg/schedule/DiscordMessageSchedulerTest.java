package com.jkky98.spubg.pubg.schedule;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import com.jkky98.spubg.discord.schedule.DiscordConnectionFailureException;
import com.jkky98.spubg.discord.schedule.DiscordMessageScheduler;
import com.jkky98.spubg.discord.service.HelpMessageBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DiscordMessageSchedulerTest {

    private static final String CHANNEL_ID = "test-channel-id";

    @Mock
    private JDA jda;

    @Mock
    private TextChannel textChannel;

    // MessageAction 대신 MessageCreateAction 사용
    @Mock
    private MessageCreateAction messageCreateAction;

    @InjectMocks
    private DiscordMessageScheduler scheduler;

    @BeforeEach
    void setUp() {
        // @Value 로 주입되는 private channelId 필드 세팅
        ReflectionTestUtils.setField(scheduler, "channelId", CHANNEL_ID);
    }

    @Test
    @DisplayName("[DiscordMessageScheduler][sendDailyMessage] 채널이 존재하면 HelpMessageBuilder.build → sendMessageEmbeds 흐름이 호출된다")
    void sendDailyMessage_channelExists_invokesMessageFlow() {
        // given
        when(jda.getTextChannelById(CHANNEL_ID)).thenReturn(textChannel);

        MessageEmbed fakeEmbed = mock(MessageEmbed.class);
        try (MockedStatic<HelpMessageBuilder> builder = mockStatic(HelpMessageBuilder.class)) {
            builder.when(HelpMessageBuilder::build).thenReturn(fakeEmbed);

            // sendMessageEmbeds 가 MessageCreateAction 을 반환하도록 모킹
            when(textChannel.sendMessageEmbeds(fakeEmbed)).thenReturn(messageCreateAction);
            // setSuppressedNotifications(true) 체인도 동일하게
            when(messageCreateAction.setSuppressedNotifications(true)).thenReturn(messageCreateAction);

            // when
            scheduler.sendDailyMessage();

            // then
            verify(jda).getTextChannelById(CHANNEL_ID);
            builder.verify(HelpMessageBuilder::build);
            verify(textChannel).sendMessageEmbeds(fakeEmbed);
            verify(messageCreateAction).setSuppressedNotifications(true);
            verify(messageCreateAction).queue();
        }
    }

    @Test
    @DisplayName("[DiscordMessageScheduler][sendDailyMessage]채널이 null 이면 DiscordConnectionFailureException 발생")
    void sendDailyMessage_channelNull_throwsException() {
        // given
        when(jda.getTextChannelById(CHANNEL_ID)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> scheduler.sendDailyMessage())
                .isInstanceOf(DiscordConnectionFailureException.class)
                .hasMessageContaining("채널을 찾을 수 없습니다");

        verify(jda).getTextChannelById(CHANNEL_ID);
        // MessageCreateAction 은 전혀 호출되지 않아야 함
        verifyNoMoreInteractions(textChannel, messageCreateAction);
    }
}
