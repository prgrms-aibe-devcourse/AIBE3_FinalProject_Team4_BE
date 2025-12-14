package com.back.domain.ai.ai.controller;

import com.back.domain.ai.ai.dto.AiChatRequest;
import com.back.domain.ai.ai.dto.AiGenerateRequest;
import com.back.domain.ai.ai.service.AiChatService;
import com.back.domain.ai.ai.service.AiGenerateService;
import com.back.domain.ai.model.exception.ModelUsageExceededException;
import com.back.domain.ai.model.service.ModelUsageService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/ais")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI API", description = "AI 관련 API")
public class ApiV1AiController {
    private final AiGenerateService aiGenerateService;
    private final AiChatService aiChatService;
    private final ModelUsageService modelUsageService;

    @PostMapping
    @Operation(summary = "블로그 제목 추천/해시태그 추천/블로그 내용 요약/키워드 추출")
    public Mono<RsData<Object>> generate(@RequestBody @Validated AiGenerateRequest req) {
        return Mono.fromCallable(() -> aiGenerateService.generate(req))
                .subscribeOn(Schedulers.boundedElastic())
                .map(RsData::successOf);
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "챗봇 (스트리밍 응답)")
    public Flux<ServerSentEvent<RsData<?>>> chat(@AuthenticationPrincipal SecurityUser userDetails,
                                                 @RequestBody @Validated AiChatRequest req) {
        Long userId = userDetails.getId();
        String model = req.model().getValue();

        log.info("CHAT HIT userId={}, model={}, contentLen={}", userId, model, req.content().length());

        // 모델 사용량 체크
        Mono<Void> check = modelUsageService.checkModelAvailability(userId, model);

        // AI 응답 스트림
        Flux<ServerSentEvent<RsData<?>>> contentStream =
                aiChatService.chatStream(req)
                        .timeout(Duration.ofSeconds(90)) // 90초 동안 아무 chunk도 안 오면 timeout
                        .map(chunk ->
                                ServerSentEvent.<RsData<?>>builder()
                                        .event("chunk")
                                        .data(RsData.successOf(chunk))
                                        .build()
                        );

        // 사용 횟수 증가
        Mono<ServerSentEvent<RsData<?>>> metaEvent = modelUsageService.increaseCountAsync(userId, model)
                        .map(meta ->
                                ServerSentEvent.<RsData<?>>builder()
                                        .event("meta")
                                        .data(RsData.successOf(meta))
                                        .build()
                        );

        return check.thenMany(contentStream)
                .concatWith(metaEvent)
                .onErrorResume(ex -> {
                    // 사용량 초과
                    if (ex instanceof ModelUsageExceededException usageEx) {
                        return Flux.just(
                                ServerSentEvent.<RsData<?>>builder()
                                        .event("error")
                                        .data(RsData.failOf("429-1", usageEx.getMessage()))
                                        .build()
                        );
                    }
                    // 그 외 오류
                    log.warn("AI 스트리밍 응답 중 오류", ex);
                    return Flux.just(
                            ServerSentEvent.<RsData<?>>builder()
                                    .event("error")
                                    .data(RsData.failOf("500-1", "AI 응답 중 오류가 발생했습니다."))
                                    .build()
                    );
                })
                .doOnCancel(() -> log.info("AI chat 클라이언트가 연결 끊음"))
                .doFinally(sig -> log.debug("AI 스트리밍 응답 끝: {}", sig));
    }

    @PostMapping(value = "/chat/once", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "챗봇 (단일 응답)")
    public Mono<ResponseEntity<RsData<String>>> chatOnce(@RequestBody @Validated AiChatRequest req) {
        return Mono.fromCallable(() -> aiChatService.chatOnce(req))
                .subscribeOn(Schedulers.boundedElastic())
                .map(RsData::successOf)
                // ResponseEntity로 Content-Type JSON 명시하여 직렬화 문제 방지
                .map(rs -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(rs))
                .doOnError(e -> log.error("AI 챗봇 에러: ", e));
    }
}
