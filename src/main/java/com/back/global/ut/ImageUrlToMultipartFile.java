package com.back.global.ut;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class ImageUrlToMultipartFile {

    private final WebClient.Builder webClientBuilder;

    ImageUrlToMultipartFile(@Qualifier("highCapacityWebClientBuilder") WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public MultipartFile convert(String imageUrl) {

        WebClient webClient = webClientBuilder.build();

        final AtomicReference<String> contentTypeRef = new AtomicReference<>(); // 외부에 ContentType을 담을 변수를 선언 (동기 처리 중이므로 가능)

        byte[] imageBytes = webClient.get()
                .uri(imageUrl)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> Mono.error(
                                new RuntimeException("이미지 URL로부터 다운로드 실패 (HTTP Status: %s)".formatted(response.statusCode().value()))
                        ))
                .toEntity(byte[].class)
                .map(response -> {
                    contentTypeRef.set(Objects.requireNonNull(response.getHeaders().getContentType()).toString());
                    return response.getBody();
                })
                .onErrorMap(throwable -> {
                    String errorMessage = "이미지 다운로드 중 오류 발생";
                    if (throwable instanceof WebClientResponseException) {
                        errorMessage = String.format("WebClient 오류 발생: %s", throwable.getMessage());
                    } else if (throwable instanceof IllegalStateException && throwable.getMessage().contains("Timeout")) {
                        errorMessage = "이미지 다운로드 중 타임아웃 발생";
                    }
                    return new RuntimeException(errorMessage, throwable);
                })
                .block();

        if (imageBytes == null) {
            throw new RuntimeException("다운로드된 이미지 데이터가 비어있습니다.");
        }

        String contentType = contentTypeRef.get();
        if (contentType == null) {
            try {
                contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(imageBytes));
            } catch (IOException e) {
                log.error("이미지 content type 스트림 분석 실패", e);
            }
            if (contentType == null) contentType = "image/jpeg";
        }

        String ext = contentType.split("/")[1];
        String fileName = "api-image-" + UUID.randomUUID() + "." + ext;

        return new MockMultipartFile(
                "image",
                fileName,
                contentType,
                imageBytes
        );
    }
}