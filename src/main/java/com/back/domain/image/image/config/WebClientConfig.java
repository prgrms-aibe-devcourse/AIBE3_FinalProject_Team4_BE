package com.back.domain.image.image.config;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    // [무료 사진 API] 타임아웃 설정
    @Bean
    public WebClient.Builder customWebClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000) // 연결 타임아웃 10초
                .responseTimeout(Duration.ofSeconds(20));               // 응답 타임아웃 20초

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    // [이미지 URL 파일로 변환] 버퍼 크기 제한 10MB로 늘림
    @Bean
    public WebClient.Builder highCapacityWebClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
                .responseTimeout(Duration.ofSeconds(20));

        return WebClient.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    // 기본
    @Bean
    @Primary
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
