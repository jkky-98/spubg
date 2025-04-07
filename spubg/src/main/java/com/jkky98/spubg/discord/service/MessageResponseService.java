package com.jkky98.spubg.discord.service;

import com.jkky98.spubg.discord.domain.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class MessageResponseService {
    public void sendHelp(MessageReceivedEvent event) {
        MessageEmbed helpMessageEmbed = HelpMessageBuilder.build();
        event.getChannel().sendMessageEmbeds(helpMessageEmbed).queue();
    }

    public void sendMembers(MessageReceivedEvent event, List<Member> members) {
        if (members.isEmpty()) {
            event.getChannel().sendMessage("❌ 등록된 멤버가 없습니다. ❌").queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("👥 멤버 목록");
        embed.setColor(Color.BLUE);

        StringBuilder memberList = new StringBuilder();
        for (Member member : members) {
            String status = (member.getDiscordName() == null) ? "(❌디스코드 등록 필요)" : "(✅디스코드 등록 완료)";
            memberList.append("• ").append(member.getUsername()).append(" ").append(status).append("\n");
        }

        embed.setDescription(memberList.toString());
        embed.setFooter("총 " + members.size() + "명의 멤버가 조회되었습니다.");

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }


    public void sendWeaponRankingTable(MessageReceivedEvent event, List<WeaponRanking> topWeaponRankings) {
        if (topWeaponRankings.isEmpty()) {
            event.getChannel().sendMessage("🏆 No rankings available.").queue();
            return;
        }

        // USERNAME 기준 정렬
        topWeaponRankings.sort(Comparator.comparing(WeaponRanking::getUsername));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🏆 Weapon Performance Rankings");
        embed.setColor(Color.YELLOW);
        embed.setFooter("📅 최신 시즌 기준 | 제작자: jkky98", "https://img.icons8.com/?size=100&id=xqPslIlorct3&format=png&color=000000");

        String currentUser = "";
        StringBuilder weaponInfo = new StringBuilder();

        for (WeaponRanking ranking : topWeaponRankings) {
            // username이 바뀌면 이전 플레이어의 필드 추가
            if (!currentUser.isEmpty() && !ranking.getUsername().equals(currentUser)) {
                embed.addField(currentUser, weaponInfo.toString(), false);
                weaponInfo.setLength(0);
            }
            // 새로운 유저면 헤더부터 추가
            if (!ranking.getUsername().equals(currentUser)) {
                weaponInfo.append("`무기이름          평균데미지    평균기절 기절거리`\n");
                currentUser = ranking.getUsername();  // 여기서 바꿔줘야 header 중복 방지됨
            }

            weaponInfo.append(String.format("`%-15s %-10d %-6.2f %-6s`%n",
                    ranking.getWeaponName(),
                    ranking.getAvgDamage(),
                    ranking.getAvgGroggy().setScale(2, RoundingMode.HALF_UP),
                    ranking.getAvgGroggyDistance()
            ));
        }
        // 마지막 플레이어 추가
        if (!currentUser.equals("")) {
            embed.addField(currentUser, weaponInfo.toString(), false);
        }

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }


    public void sendHeadShotTable(MessageReceivedEvent event, List<HeadshotRanking> headshotRankings) {
        if (headshotRankings.isEmpty()) {
            event.getChannel().sendMessage("🎯 No headshot rankings available.").queue();
            return;
        }

        StringBuilder tableBuilder = new StringBuilder("```");
        tableBuilder.append("🎯 **Headshot Performance Rankings** 🎯\n");
        tableBuilder.append("Top players ranked by headshot performance.\n\n");
        tableBuilder.append(String.format("%-12s %-10s %-10s %-10s %-5s%n", "PLAYER", "HITS", "SHOTS", "RATIO(%)", "RANK"));
        tableBuilder.append("-------------------------------------------------------------\n");

        for (HeadshotRanking ranking : headshotRankings) {
            tableBuilder.append(String.format("%-12s %-10d %-10d %-10.2f %-5d%n",
                    ranking.getUsername(),
                    ranking.getHeadshotCount(),
                    ranking.getTotalDamageCount(),
                    ranking.getHeadshotRatio(),
                    ranking.getRanking()
            ));
        }
        tableBuilder.append("```");

        // Embed 메시지로 랭킹 제목과 설명을 추가
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🎯 **Headshot Performance Rankings** 🎯");
        embed.setDescription("이 랭킹은 각 플레이어의 헤드샷 성능에 따라 측정됩니다.\n" +
                "헤드샷을 많이 기록할수록 높은 순위를 차지합니다.");
        embed.setColor(Color.GREEN);
        embed.addField("📊 데이터 기준", "헤드샷 횟수와 총 발사 횟수를 반영하여 비율을 계산", false);
        embed.setFooter("📅 최신 데이터 기준 | 제작자: jkky98", "https://img.icons8.com/?size=100&id=xqPslIlorct3&format=png&color=000000");

        // 임베드 메시지 전송
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        // 표 형태의 텍스트 메시지 전송
        event.getChannel().sendMessage(tableBuilder.toString()).queue();
    }

    public void sendGrenadeRankings(MessageReceivedEvent event, List<GrenadeRanking> grenadeRankings) {
        if (grenadeRankings.isEmpty()) {
            event.getChannel().sendMessage("💣 No grenade rankings available.").queue();
            return;
        }

        grenadeRankings.sort(Comparator.comparing(GrenadeRanking::getWeightedScore).reversed());

        StringBuilder tableBuilder = new StringBuilder("```");
        tableBuilder.append("🔥 **Grenade Performance Rankings** 🔥\n");
        tableBuilder.append("Top players ranked by grenade performance (weighted score).\n\n");
        tableBuilder.append(String.format("%-5s %-15s %-10s %-10s %-15s %-10s%n", "🏆 Rank", "Player", "Count", "Damage", "Avg Damage", "Score"));
        tableBuilder.append("---------------------------------------------------------------\n");

        for (GrenadeRanking ranking : grenadeRankings) {
            tableBuilder.append(String.format("%-5d %-15s %-10d %-10.1f %-15.2f %-10.2f%n",
                    ranking.getRanking(),
                    ranking.getUsername(),
                    ranking.getTotalGrenadeCount(),
                    ranking.getTotalGrenadeDamage(),
                    ranking.getAvgDamagePerGrenade(),
                    ranking.getWeightedScore() // ✅ 가중 점수 반영
            ));
        }
        tableBuilder.append("```");

        // Embed 메시지로 랭킹 제목과 설명을 추가
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("💣 **Grenade Performance Rankings** 💣");
        embed.setDescription("이 랭킹은 수류탄의 가중 점수에 따라 순위가 매겨진 결과입니다.\n" +
                "수류탄을 사용할 때마다 총 데미지, 평균 데미지 등을 반영하여 계산됩니다.");
        embed.setColor(Color.RED);
        embed.addField("📊 데이터 기준", "총 사용 횟수와 평균 데미지를 기준으로 가중치 적용", false);
        embed.setFooter("📅 최신 데이터 기준 | 제작자: jkky98", "https://img.icons8.com/?size=100&id=xqPslIlorct3&format=png&color=000000");

        // 임베드 메시지 전송
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        // 표 형태의 텍스트 메시지 전송
        event.getChannel().sendMessage(tableBuilder.toString()).queue();
    }


    public void sendLongDistance(MessageReceivedEvent event, List<LongDistanceRanking> longDistanceRankings) {
        if (longDistanceRankings.isEmpty()) {
            event.getChannel().sendMessage("🏹 No long-distance rankings available.").queue();
            return;
        }

        // ✅ RANKING 기준으로 정렬
        longDistanceRankings.sort(Comparator.comparingInt(LongDistanceRanking::getRanking));

        StringBuilder tableBuilder = new StringBuilder("```");
        tableBuilder.append(String.format("%-5s %-15s %-10s%n", "🏆 Rank", "Player", "Avg Distance"));
        tableBuilder.append("-------------------------------------\n");

        for (LongDistanceRanking ranking : longDistanceRankings) {
            tableBuilder.append(String.format("%-5d %-15s %.2fm%n",
                    ranking.getRanking(),
                    ranking.getUsername(),
                    ranking.getAvgHitDistance()
            ));
        }
        tableBuilder.append("```");

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🏹 **Long Distance Master Rankings** 🏹");
        embed.setDescription("이 랭킹은 유효한 타격 거리(히트 거리)를 기반으로 측정됩니다.\n" +
                "거리가 길수록 높은 랭킹을 차지합니다.");
        embed.setColor(Color.CYAN);
        embed.addField("📊 데이터 기준", "NONE, NONSPECIFIED 데미지 제외", false);
        embed.setFooter("📅 최신 데이터 기준 | 제작자: jkky98", "https://img.icons8.com/?size=100&id=xqPslIlorct3&format=png&color=000000");

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        event.getChannel().sendMessage(tableBuilder.toString()).queue();
    }


    public void sendRidingRankings(MessageReceivedEvent event, List<RidingRanking> ridingRankings) {
        if (ridingRankings.isEmpty()) {
            event.getChannel().sendMessage("🚗 No riding rankings available.").queue();
            return;
        }

        // 🚀 랭킹 기준으로 정렬 (낮은 번호가 상위)
        ridingRankings.sort(Comparator.comparingInt(RidingRanking::getRanking));

        // 코드 블록 내에 테이블 형태로 랭킹 정보를 구성합니다.
        StringBuilder tableBuilder = new StringBuilder("```");
        tableBuilder.append(String.format("%-5s %-15s %-15s%n", "🏆 Rank", "Player", "Riding Damage"));
        tableBuilder.append("--------------------------------------------\n");

        for (RidingRanking ranking : ridingRankings) {
            tableBuilder.append(String.format("%-5d %-15s %.2f%n",
                    ranking.getRanking(),
                    ranking.getUsername(),
                    ranking.getRidingDamage()
            ));
        }
        tableBuilder.append("```");

        // Embed 메시지 생성
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🚗 **Riding Damage Rankings** 🚗");
        embed.setDescription("이 랭킹은 차량 탑승 상태에서의 공격 데미지를 기준으로 측정됩니다.\n" +
                "데미지가 높을수록 순위가 높습니다.");
        embed.setColor(Color.ORANGE);
        embed.addField("📊 데이터 기준", "모든 차량 탑승 공격 데미지 기준", false);
        embed.setFooter("📅 최신 데이터 기준 | 제작자: jkky98", "https://img.icons8.com/?size=100&id=xqPslIlorct3&format=png&color=000000");

        // 임베드 메시지와 테이블 메시지를 차례로 전송합니다.
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        event.getChannel().sendMessage(tableBuilder.toString()).queue();
    }

    public void sendLotOfFireRanking(MessageReceivedEvent event, List<LotOfFireRanking> lotOfFireRankings) {
        if (lotOfFireRankings.isEmpty()) {
            event.getChannel().sendMessage("🔥 No fire rankings available.").queue();
            return;
        }

        lotOfFireRankings.sort(Comparator.comparingInt(LotOfFireRanking::getRanking));

        StringBuilder tableBuilder = new StringBuilder("```");
        tableBuilder.append("🔥 **Lot of Fire Rankings** 🔥\n");
        tableBuilder.append("Top players ranked by total fire usage.\n\n");
        tableBuilder.append(String.format("%-15s %-10s %-5s%n", "PLAYER", "FIRE", "RANK"));
        tableBuilder.append("------------------------------------------------------\n");

        for (LotOfFireRanking ranking : lotOfFireRankings) {
            tableBuilder.append(String.format("%-15s %-10d %-5d%n",
                    ranking.getUsername(),
                    ranking.getFire(),
                    ranking.getRanking()
            ));
        }
        tableBuilder.append("```");

        // Embed 메시지로 랭킹 제목과 설명을 추가
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🔥 **Lot of Fire Rankings** 🔥");
        embed.setDescription("이 랭킹은 각 플레이어가 사용한 총 'fire' 횟수를 기준으로 측정됩니다.\n" +
                "한 매치에서 더 많이 탄환을 발사한 플레이어가 높은 순위를 차지합니다.");
        embed.setColor(Color.ORANGE);
        embed.addField("📊 데이터 기준", "각 게임에서 사용된 총 'fire' 횟수를 기준으로 순위 계산", false);
        embed.setFooter("📅 최신 데이터 기준 | 제작자: jkky98", "https://img.icons8.com/?size=100&id=xqPslIlorct3&format=png&color=000000");

        // 임베드 메시지 전송
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        // 표 형태의 텍스트 메시지 전송
        event.getChannel().sendMessage(tableBuilder.toString()).queue();
    }

    public void sendPhaseDealtRanking(MessageReceivedEvent event, List<PhaseDealtRanking> phaseDealtRankings) {
        if (phaseDealtRankings.isEmpty()) {
            event.getChannel().sendMessage("🚀 No phase dealt rankings available.").queue();
            return;
        }

        phaseDealtRankings.sort(Comparator.comparingDouble((PhaseDealtRanking r) -> r.getAvgDealt() / r.getWeightDamage()).reversed());

        // 정렬된 순서대로 순위 재설정 (1부터 시작)
        int rankCounter = 1;
        for (PhaseDealtRanking ranking : phaseDealtRankings) {
            ranking.setRanking(rankCounter++);
        }

        StringBuilder tableBuilder = new StringBuilder("```");
        tableBuilder.append("🚀 **Phase Dealt Rankings** 🚀\n");
        tableBuilder.append("Ranked by AVG DEALT / WEIGHT DAMAGE ratio.\n\n");
        tableBuilder.append(String.format("%-15s %-15s %-15s %-10s %-5s%n", "PLAYER", "AVG DEALT", "WEIGHT DAMAGE", "RATIO", "RANK"));
        tableBuilder.append("-----------------------------------------------------------------\n");

        for (PhaseDealtRanking ranking : phaseDealtRankings) {
            double ratio = ranking.getAvgDealt() / ranking.getWeightDamage();
            tableBuilder.append(String.format("%-15s %-15.2f %-15.2f %-10.3f %-5d%n",
                    ranking.getUsername(),
                    ranking.getAvgDealt(),
                    ranking.getWeightDamage(),
                    ratio,
                    ranking.getRanking()
            ));
        }
        tableBuilder.append("```");

        // 임베드 메시지 구성
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🚀 **Phase Dealt Rankings** 🚀");
        embed.setDescription("이 랭킹은 각 플레이어의 AVG DEALT와 WEIGHT DAMAGE의 비율(AVG DEALT / WEIGHT DAMAGE)을 기준으로 측정됩니다.\n" +
                "비율이 높을수록 후반 페이즈에서의 성과가 우수함을 의미합니다.");
        embed.setColor(Color.MAGENTA);
        embed.addField("📊 데이터 기준", "AVG DEALT / WEIGHT DAMAGE", false);
        embed.setFooter("📅 최신 데이터 기준 | 제작자: jkky98", "https://img.icons8.com/?size=100&id=xqPslIlorct3&format=png&color=000000");

        // 임베드 메시지 전송
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        // 테이블 형식의 텍스트 메시지 전송
        event.getChannel().sendMessage(tableBuilder.toString()).queue();
    }


    public void sendClutchDealtRanking(MessageReceivedEvent event, List<ClutchDealtRanking> clutchDealtRankings) {
        if (clutchDealtRankings.isEmpty()) {
            event.getChannel().sendMessage("💥 No clutch dealt rankings available.").queue();
            return;
        }

        clutchDealtRankings.sort(Comparator.comparingInt(ClutchDealtRanking::getRanking));

        StringBuilder tableBuilder = new StringBuilder("```");
        tableBuilder.append("💥 **Clutch Dealt Rankings** 💥\n");
        tableBuilder.append("Top players ranked by clutch dealt performance.\n\n");
        tableBuilder.append(String.format("%-15s %-15s %-5s%n", "PLAYER", "CLUTCH VALUE", "RANK"));
        tableBuilder.append("-------------------------------------------\n");

        for (ClutchDealtRanking ranking : clutchDealtRankings) {
            tableBuilder.append(String.format("%-15s %-15.2f %-5d%n",
                    ranking.getUsername(),
                    ranking.getClutchDealt(),
                    ranking.getRanking()
            ));
        }
        tableBuilder.append("```");

        // 임베드 메시지 구성
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("💥 **Clutch Dealt Rankings** 💥");
        embed.setDescription("이 랭킹은 자신의 딜량 대비 체력이 낮은 상황에서의 딜량을 기준으로 측정됩니다.\n" +
                "높은 클러치 딜 비율값은 자신의 위기 상황에서도 `어김없이 총을 쏠 수 있는 성향`을 나타냅니다.\n" +
                "이 값은 딜량랭킹과 무관하며 성향을 드러냅니다. **팀을 위해 자신의 죽음을 무릎쓴 것일까요? 혹은 굳이 팀의 위기가 아닌데도 위험하게 플레이 한 것일까요?**\n" +
                "\n"+
                "***이 성향 값이 높게 측정되면서 딜량이 낮다면 안전한 플레이로의 고민을 해보세요!***"
        );
        embed.setColor(Color.RED);
        embed.addField("📊 데이터 기준", "클러치 상황에서의 딜 평균(가중치 적용)", false);
        embed.setFooter("📅 최신 데이터 기준 | 제작자: jkky98", "https://img.icons8.com/?size=100&id=xqPslIlorct3&format=png&color=000000");

        // 임베드 메시지와 테이블 메시지 전송
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        event.getChannel().sendMessage(tableBuilder.toString()).queue();
    }

    public void sendGroggyRanking(MessageReceivedEvent event, List<GroggyRanking> groggyRankings) {
        if (groggyRankings.isEmpty()) {
            event.getChannel().sendMessage("💥 No groggy rankings available.").queue();
            return;
        }

        groggyRankings.sort(Comparator.comparingInt(GroggyRanking::getRanking));

        StringBuilder tableBuilder = new StringBuilder("```");
        tableBuilder.append("💥 **Groggy Rankings** 💥\n");
        tableBuilder.append("Top players ranked by groggy performance.\n\n");
        tableBuilder.append(String.format("%-15s %-10s %-15s %-5s%n", "PLAYER", "MATCHES", "GROGGY per Match", "RANK"));
        tableBuilder.append("-----------------------------------------------------------\n");

        for (GroggyRanking ranking : groggyRankings) {
            tableBuilder.append(String.format("%-15s %-10d %-15.2f %-5d%n",
                    ranking.getUsername(),
                    ranking.getTotalMatches(),
                    ranking.getGroggyRatio(),
                    ranking.getRanking()
            ));
        }
        tableBuilder.append("```");

        // 임베드 메시지 구성
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("💥 **Groggy Rankings** 💥");
        embed.setDescription("이 랭킹은 플레이어가 참가한 전체 경기 대비 상대를 기절시킨 경기의 비율을 기반으로 산출됩니다.\n" +
                "팀의 메인 공격수 포지션을 볼 수 있어요!");
        embed.setColor(Color.ORANGE);
        embed.addField("📊 데이터 기준", "총 경기 수 대비 기절(%)", false);
        embed.setFooter("📅 Latest Data | Created by: jkky98", "https://img.icons8.com/?size=100&id=xqPslIlorct3&format=png&color=000000");

        // 임베드 메시지와 테이블 메시지 전송
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        event.getChannel().sendMessage(tableBuilder.toString()).queue();
    }

    public void sendSmokeRanking(MessageReceivedEvent event, List<SmokeRanking> smokeRankings) {
        if (smokeRankings.isEmpty()) {
            event.getChannel().sendMessage("🌫 No smoke rankings available.").queue();
            return;
        }

        smokeRankings.sort(Comparator.comparingInt(SmokeRanking::getRanking));

        StringBuilder tableBuilder = new StringBuilder("```");
        tableBuilder.append("🌫 **Smoke Usage Rankings** 🌫\n");
        tableBuilder.append("Top players ranked by smoke usage per match.\n\n");
        tableBuilder.append(String.format("%-15s %-10s %-15s %-5s%n", "PLAYER", "WEAPON", "SMOKE per Match", "RANK"));
        tableBuilder.append("-----------------------------------------------------------\n");

        for (SmokeRanking ranking : smokeRankings) {
            tableBuilder.append(String.format("%-15s %-10s %-15.2f %-5d%n",
                    ranking.getUsername(),
                    ranking.getWeaponName(),
                    ranking.getPerMatch().setScale(2, RoundingMode.HALF_UP),
                    ranking.getRanking()
            ));
        }
        tableBuilder.append("```");

        // 임베드 메시지 구성
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🌫 **Smoke Usage Rankings** 🌫");
        embed.setDescription("이 랭킹은 플레이어가 한 매치당 얼마나 자주 스모크 무기를 사용했는지를 보여줍니다.\n" +
                "`SMOKE_GRENADE`, `M79` 등을 포함한 스모크류 무기의 사용 빈도로 랭킹이 매겨집니다.");
        embed.setColor(Color.GRAY);
        embed.addField("📊 데이터 기준", "한 경기당 스모크 무기 사용 횟수", false);
        embed.setFooter("📅 Latest Data | Created by: jkky98", "https://img.icons8.com/?size=100&id=xqPslIlorct3&format=png&color=000000");

        // 임베드와 랭킹 테이블 전송
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        event.getChannel().sendMessage(tableBuilder.toString()).queue();
    }

}
