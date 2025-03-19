package com.jkky98.spubg.discord.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
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
            channel.sendMessageEmbeds(buildHelpEmbed().build())
                    .setSuppressedNotifications(true)
                    .queue();
        } else {
            log.error("⚠\uFE0F 채널을 찾을 수 없습니다. 채널 ID를 확인하세요: {}", channelId);
        }
    }

    // 📜 도움말 메시지 생성 (EmbedBuilder 활용)
    private EmbedBuilder buildHelpEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("**📜 명령어 도움말 📜**");
        embed.setDescription("**아래는 SPUBG BOT에서 사용 가능한 명령어 목록입니다.**");
        embed.setDescription("**현재 베타서비스는 스팀, 스쿼드 게임의 데이터만 처리하고 있습니다.**");
        embed.setDescription("**명령어 작성시 {...} 은 변수입니다. '{', '}' 를 빼고 작성하면 됩니다.**");
        embed.setColor(Color.ORANGE);
        embed.addField("!도움", "봇 사용법에 대해 알려드립니다, 명령어 목록을 표시합니다.", false);
        embed.addField("!등록 {배그닉네임}", "디스코드 닉네임과 서버에 등록된 PUBG 플레이어 명을 매핑합니다.", false);
        embed.addField("!멤버", "현재 봇이 스탯을 관리하는 멤버들을 알려드립니다.", false);
        embed.addField("!웨폰마스터", "이번 시즌 총기 및 투척 별 1등을 나타냅니다. \n (거리 및 클러치 상황은 가중 반영됩니다. 발사당 가한 데미지를 기본으로 판단합니다.) \n (총알을 낭비하는 경우 이 지표는 좋지 않게 나타날 수 있습니다.)", false);
        embed.addField("!헤드슈터", "팀의 헤드 슈터는 누굴까요?", false);
        embed.addField("!감자왕", "수류탄을 많이 챙겨도 좋은 사람입니다! 그에게 수류탄을 몰아주세요!", false);
        embed.addField("!장거리러버", "DMR을 잘 맞추는 사람인가봐요. 팀의 포탑이라고 칭해도 좋아요!", false);
        embed.addField("!라이딩샷마스터", "라이딩샷 딜량왕은 누굴까요? 팀의 기마대장입니다!", false);
        embed.addField("!발사왕", "총알을 많이 쓰는 사람은 누굴까요? 차에 총알을 잘 실어놓지 않으면 팀원의 총알이 거덜날거에요!", false);
        embed.addField("!후반딜러", "어려운 환경속! 후반 페이즈록 갈 수록 딜량을 잘 뽑아내는 사람은 누굴까요?(평균 딜량 대비 후반 페이즈 딜량으로 계산됩니다.)", false);
        embed.addField("!최근게임딜량그래프", "\uD83D\uDEE0\uFE0F개발 예정\uD83D\uDEE0\uFE0F", false);


        embed.setFooter("제작자: jkky98 - aal2525@ajou.ac.kr", "https://img.icons8.com/?size=100&id=xqPslIlorct3&format=png&color=000000");

        return embed;
    }
}

