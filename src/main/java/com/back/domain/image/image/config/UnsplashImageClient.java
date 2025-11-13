package com.back.domain.image.image.config;

import com.back.domain.image.image.dto.UnsplashImageSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UnsplashImageClient {

    private final WebClient webClient;
    private final String accessKey;

    public UnsplashImageClient(WebClient.Builder webClientBuilder,
                               @Value("${unsplash.base-url}") String baseUrl,
                               @Value("${unsplash.access-key}") String accessKey) {

        this.accessKey = accessKey;

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Client-ID " + this.accessKey)
                .build();
    }

    public UnsplashImageSearchResult searchImages(String query, int page, int perPage) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/photos")
                        .queryParam("query", query)
                        .queryParam("page", page)        // 기본값: 1
                        .queryParam("per_page", perPage) // 기본값: 10
                        .build())
                .retrieve()

                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> Mono.error(new RuntimeException("Unsplash API 호출 오류: " + clientResponse.statusCode())))

                .bodyToMono(UnsplashImageSearchResult.class)
                .block();
    }
}
