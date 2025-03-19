package com.jkky98.spubg.discord.command;

import com.jkky98.spubg.discord.Command;
import com.jkky98.spubg.discord.repository.MemberMapper;
import com.jkky98.spubg.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterCommand implements Command {

    private final MemberMapper memberMapper;

    @Override
    public String getCommandName() {
        return "등록";
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("❌ 올바른 형식: `!등록 <게임 닉네임>`").queue();
            return;
        }

        String discordName = event.getAuthor().getName(); // 디스코드 닉네임
        String username = args[0]; // 게임 닉네임

        Member member = memberMapper.findByUsername(username);

        if (member == null) {
            event.getChannel().sendMessage("❌ '" + username + "' 닉네임을 가진 멤버를 찾을 수 없습니다.").queue();
            return;
        }

        // discordName 업데이트
        member.setDiscordName(discordName);
        memberMapper.update(member); // MyBatis에서 update 메서드 필요

        event.getChannel().sendMessage("✅ " + username + " 님의 디스코드 계정이 '" + discordName + "' 으로 등록되었습니다!").queue();
    }
}
