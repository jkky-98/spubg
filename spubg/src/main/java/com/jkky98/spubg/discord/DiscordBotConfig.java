package com.jkky98.spubg.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordBotConfig {

    @Value("${discord.bot.token}")
    private String token;

    @Bean
    public JDA jda(DiscordCommandListener discordCommandListener) throws InterruptedException {
        return JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT) // ✅ 메시지 읽기 활성화
                .addEventListeners(discordCommandListener)
                .build()
                .awaitReady();
    }
}
