package com.jkky98.spubg.pubg.request;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient() {
        return WebClient
                .builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(30 * 1024 * 1024)) // 20MB 제한 증가
                .build();
    }
}
