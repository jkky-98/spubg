package com.jkky98.spubg.pubg.request;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient() {

        return WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // 연결 대기 10초
                        .responseTimeout(Duration.ofSeconds(10)) // 응답 대기 10초
                        .compress(true)
                ))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024)) // 50MB 제한 증가
                .build();
    }
}
