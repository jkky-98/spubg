package com.jkky98.spubg.pubg;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class DiscordConnectionTest {

    @Autowired
    JDA jda;

    @Value("${discord.channel.id}")
    String channelId;

    @Test
    @DisplayName("[JDA][getTextChannelById] 채널 연결 성공 테스트")
    void testSendDailyMessageConnenctionSuccess() {
        // given
        // when
        TextChannel channel = jda.getTextChannelById(channelId);
        // then
        assertThat(channel).isNotNull();
    }

    @Test
    @DisplayName("[JDA][getTextChannelById] 채널 연결 실패 테스트 :: NumberFormatException")
    void testSendDailyMessageConnectionFailureString() {
        // given
        String channelIdFailure = "fail";
        // when
        // then
        assertThatThrownBy(() -> jda.getTextChannelById(channelIdFailure)).isInstanceOf(NumberFormatException.class);
    }

    @Test
    @DisplayName("[JDA][getTextChannelById] 채널 연결 실패 테스트 :: Null")
    void testSendDailyMessageConnectionFailureNumber() {
        // given
        String channelIdFailure = "12345";
        // when
        TextChannel textChannelById = jda.getTextChannelById(channelIdFailure);
        // then
        assertThat(textChannelById).isNull();
    }
}
