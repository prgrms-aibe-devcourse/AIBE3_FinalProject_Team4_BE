package com.back.domain.image.image;

import com.back.domain.image.image.dto.GoogleSearchResponse;
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

    /**
     * Google Custom Search API를 호출하여 이미지를 검색합니다.
     * @param query 검색어
     * @return GoogleSearchResponse 객체 (내부에 List<GoogleImageItem> 포함)
     */
    public GoogleSearchResponse searchImages(String query) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", apiKey) // API Key
                        .queryParam("cx", cxId)   // Search Engine ID
                        .queryParam("searchType", "image") // 이미지 검색을 명시
                        .queryParam("q", query)    // 검색어
                        .queryParam("num", 10)     // 결과 개수 (최대 10개)
                        .build())
                .retrieve()

                // 오류 처리: 4xx, 5xx 에러 발생 시 예외 처리
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> Mono.error(new RuntimeException("Google API 호출 오류: " + clientResponse.statusCode())))

                // GoogleSearchResponse 타입으로 매핑
                .bodyToMono(GoogleSearchResponse.class)
                .block();
    }
}
