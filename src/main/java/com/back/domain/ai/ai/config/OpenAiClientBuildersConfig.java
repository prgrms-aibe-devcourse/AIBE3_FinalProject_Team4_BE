package com.back.domain.ai.ai.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class OpenAiClientBuildersConfig {

    @Bean("openAiWebClientBuilder")
    public WebClient.Builder openAiWebClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                // 연결 타임아웃 (TCP connect)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
                // 서버 응답 타임아웃 (첫 응답/전체 응답 관점)
                .responseTimeout(Duration.ofSeconds(120))
                .doOnConnected(conn -> conn
                        // 읽기/쓰기 타임아웃(소켓 레벨)
                        .addHandlerLast(new ReadTimeoutHandler(120))
                        .addHandlerLast(new WriteTimeoutHandler(120))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    @Bean("openAiRestClientBuilder")
    public RestClient.Builder openAiRestClientBuilder() {
        // Apache HC5를 쓴다면 HttpComponentsClientHttpRequestFactory으로
        var requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5_000);
        requestFactory.setReadTimeout(120_000);

        return RestClient.builder()
                .requestFactory(requestFactory);
    }
}
