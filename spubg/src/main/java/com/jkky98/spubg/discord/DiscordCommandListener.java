package com.jkky98.spubg.discord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordCommandListener extends ListenerAdapter {
    private final CommandInvoker commandInvoker;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // 봇이 보낸 메시지 무시
        if (event.getAuthor().isBot()) return;

        // 메시지 공백 기준 분리
        String[] args = event.getMessage().getContentRaw().split(" ");

        // 명령어가 비어있거나 "!"로 시작하지 않으면 종료
        if (args.length == 0 || !args[0].startsWith("!")) return;

        // 명령어 추출 (! 제거)
        String command = args[0].substring(1);

        // 추가 인자가 있을 경우 저장, 없으면 빈 배열로 설정
        String[] arguments = Arrays.copyOfRange(args, 1, args.length);

        // 명령어 실행
        commandInvoker.handleCommand(command, event, arguments);
    }

}
