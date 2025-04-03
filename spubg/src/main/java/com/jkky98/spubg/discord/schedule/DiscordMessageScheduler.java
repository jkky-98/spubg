package com.jkky98.spubg.discord.schedule;

import com.jkky98.spubg.discord.service.HelpMessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscordMessageScheduler {

    private final JDA jda;

    @Value("${discord.channel.id}")
    private String channelId;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul") // 매일 저녁 8시 실행
    public void sendDailyMessage() {
        TextChannel channel = jda.getTextChannelById(channelId); // 메시지를 보낼 채널 ID
        if (channel != null) {
            MessageEmbed helpMessageEmbed = HelpMessageBuilder.build();
            channel.sendMessageEmbeds(helpMessageEmbed)
                    .setSuppressedNotifications(true)
                    .queue();
        } else {
            log.error("⚠\uFE0F 채널을 찾을 수 없습니다. 채널 ID를 확인하세요: {}", channelId);
        }
    }
}

