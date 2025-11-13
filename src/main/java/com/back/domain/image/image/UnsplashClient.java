package com.back.domain.image.image;

import com.back.domain.image.image.dto.UnsplashSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UnsplashClient {

    private final WebClient webClient;
    private final String accessKey;

    public UnsplashClient(WebClient.Builder webClientBuilder,
                          @Value("${unsplash.base-url}") String baseUrl,
                          @Value("${unsplash.access-key}") String accessKey) {

        this.accessKey = accessKey;

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Client-ID " + this.accessKey)
                .build();
    }

    /**
     * Unsplash API를 호출하여 이미지를 검색하고, 매핑된 결과를 반환합니다.
     *
     * @param query 검색어
     * @return UnsplashSearchResult 객체 (내부에 List<UnsplashPhoto> 포함)
     */
    public UnsplashSearchResult searchImages(String query) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/photos")
                        .queryParam("query", query)
                        .queryParam("per_page", 10)
                        .build())
                .retrieve()

                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> Mono.error(new RuntimeException("Unsplash API 호출 오류: " + clientResponse.statusCode())))

                .bodyToMono(UnsplashSearchResult.class)
                .block();
    }
}
