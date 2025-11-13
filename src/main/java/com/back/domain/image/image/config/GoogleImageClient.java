package com.back.domain.image.image.config;

import com.back.domain.image.image.dto.GoogleImageSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class GoogleImageClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String cxId;

    public GoogleImageClient(WebClient.Builder webClientBuilder,
                             @Value("${google.base-url}") String baseUrl,
                             @Value("${google.api-key}") String apiKey,
                             @Value("${google.cx-id}") String cxId) {

        this.apiKey = apiKey;
        this.cxId = cxId;

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public GoogleImageSearchResult searchImages(String query, int page, int num) {
        final int MAX_RESULTS_LIMIT = 100;               // 최대 반환 개수: 100개
        int finalNum = Math.min(num, 10);               // 반환할 검색결과 수: 1에서 10 사이의 정수
        int startIndex = (page - 1) * finalNum + 1;      // 반환할 첫 번째 결과의 색인

        if (startIndex > MAX_RESULTS_LIMIT) {
            throw new IllegalArgumentException("페이지 번호가 너무 큽니다. * Google API는 최대 100개의 결과만 반환합니다.");
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", apiKey)
                        .queryParam("cx", cxId)
                        .queryParam("searchType", "image")
                        .queryParam("q", query)
                        .queryParam("start", startIndex)
                        .queryParam("num", finalNum)
                        .build())
                .retrieve()

                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> Mono.error(new RuntimeException("Google API 호출 오류: " + clientResponse.statusCode())))

                .bodyToMono(GoogleImageSearchResult.class)
                .block();
    }
}
