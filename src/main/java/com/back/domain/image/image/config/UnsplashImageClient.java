package com.back.domain.image.image.config;

import com.back.domain.image.image.dto.UnsplashImageSearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
public class UnsplashImageClient {

    private final WebClient webClient;

    public UnsplashImageClient(@Qualifier("customWebClientBuilder") WebClient.Builder webClientBuilder,
                               @Value("${unsplash.base-url}") String baseUrl,
                               @Value("${unsplash.access-key}") String accessKey) {

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Client-ID " + accessKey)
                .build();
    }

    public UnsplashImageSearchResult searchImages(String query, int page, int size) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/photos")
                        .queryParam("query", query)
                        .queryParam("page", page)     // 기본값: 1
                        .queryParam("per_page", size) // 기본값: 10
                        .build())
                .retrieve()

                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> Mono.error(new RuntimeException("Unsplash API 호출 오류: 상태 코드 " + clientResponse.statusCode())))

                .bodyToMono(UnsplashImageSearchResult.class)
                .timeout(Duration.ofSeconds(20))

                .onErrorResume(throwable -> {
                    if (throwable instanceof WebClientResponseException e) {
                        log.error("Unsplash API HTTP 오류: 상태 코드 {}, 응답 {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
                    } else {
                        log.error("Unsplash API 네트워크/타임아웃 등 오류: {}", throwable.getMessage(), throwable);
                    }
                    return Mono.just(new UnsplashImageSearchResult());
                })
                .block();
    }
}
