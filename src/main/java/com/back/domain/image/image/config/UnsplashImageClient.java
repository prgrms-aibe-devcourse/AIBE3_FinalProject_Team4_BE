package com.back.domain.image.image.config;

import com.back.domain.image.image.dto.UnsplashImageSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UnsplashImageClient {

    private final WebClient webClient;

    public UnsplashImageClient(WebClient.Builder webClientBuilder,
                               @Value("${unsplash.base-url}") String baseUrl,
                               @Value("${unsplash.access-key}") String accessKey) {

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Client-ID " + accessKey)
                .build();
    }

    public UnsplashImageSearchResult searchImages(String query, int page, int size) {

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("검색 키워드는 필수 항목이며 공백만으로는 검색할 수 없습니다.");
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/photos")
                        .queryParam("query", query)
                        .queryParam("page", page)     // 기본값: 1
                        .queryParam("per_page", size) // 기본값: 10
                        .build())
                .retrieve()

                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> Mono.error(new RuntimeException("Unsplash API 호출 오류: " + clientResponse.statusCode())))

                .bodyToMono(UnsplashImageSearchResult.class)
                .block();
    }
}
