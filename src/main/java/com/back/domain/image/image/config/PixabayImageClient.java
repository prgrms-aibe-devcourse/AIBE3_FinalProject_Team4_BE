package com.back.domain.image.image.config;

import com.back.domain.image.image.dto.PixabayImageSearchResult;
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
public class PixabayImageClient {

    private final WebClient webClient;
    private final String accessKey;

    public PixabayImageClient(
            @Qualifier("customWebClientBuilder") WebClient.Builder webClientBuilder,
            @Value("${pixabay.base-url}") String baseUrl,      // ex) https://pixabay.com
            @Value("${pixabay.access-key}") String accessKey
    ) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
        this.accessKey = accessKey;
    }

    public PixabayImageSearchResult searchImages(String query, int page, int size) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/")                 // Pixabay 이미지 검색 엔드포인트
                        .queryParam("key", accessKey)  // 인증키는 key 파라미터로
                        .queryParam("q", query)        // 검색어
                        .queryParam("page", page)      // 기본값 1
                        .queryParam("per_page", size)  // 기본값 20, 3~200
                         .queryParam("image_type", "all") // all/photo/illustration/vector
                         .queryParam("safesearch", "true")
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> Mono.error(
                                new RuntimeException("Pixabay API 호출 오류: 상태 코드 " + clientResponse.statusCode())
                        ))
                .bodyToMono(PixabayImageSearchResult.class)
                .timeout(Duration.ofSeconds(20))
                .onErrorResume(throwable -> {
                    if (throwable instanceof WebClientResponseException e) {
                        log.error("Pixabay API HTTP 오류: 상태 코드 {}, 응답 {}",
                                e.getStatusCode(), e.getResponseBodyAsString(), e);
                    } else {
                        log.error("Pixabay API 네트워크/타임아웃 오류: {}",
                                throwable.getMessage(), throwable);
                    }
                    return Mono.just(new PixabayImageSearchResult());
                })
                .block();
    }
}

