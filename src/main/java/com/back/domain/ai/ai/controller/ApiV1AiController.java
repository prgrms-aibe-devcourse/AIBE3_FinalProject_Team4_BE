package com.back.domain.ai.ai.controller;

import com.back.domain.ai.ai.dto.AiChatReqBody;
import com.back.domain.ai.ai.dto.AiGenerateReqBody;
import com.back.domain.ai.ai.dto.AiIndexBlogReqBody;
import com.back.domain.ai.ai.service.AiChatService;
import com.back.domain.ai.ai.service.AiGenerateService;
import com.back.domain.ai.ai.service.AiIndexService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/v1/ais")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI API", description = "AI 관련 API")
public class ApiV1AiController {
    private final AiGenerateService aiGenerateService;
    private final AiIndexService aiIndexService;
    private final AiChatService aiChatService;

    @PostMapping
    @Operation(summary = "블로그 제목 추천/해시태그 추천/블로그 내용 요약/키워드 추출")
    public Mono<RsData<Object>> generate(@RequestBody @Validated AiGenerateReqBody req) {
        return Mono.fromCallable(() -> aiGenerateService.generate(req))
                .subscribeOn(Schedulers.boundedElastic())
                .map(RsData::successOf)
                .doOnError(e -> log.error("AI 생성 기능 관련 에러", e));
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "챗봇 (스트리밍 응답)")
    public Flux<RsData<String>> chat(@RequestBody @Validated AiChatReqBody req) {
        return aiChatService.chatStream(req)
                .map(RsData::successOf)
                .doOnError(e -> log.error("AI 챗봇 (스트리밍) 에러: ", e))
                .doOnCancel(() -> log.info("클라이언트가 AI 요청 중단"));
    }

    // 챗봇 기능은 스트리밍 응답을 기본으로 할 예정입니다.
    @PostMapping(value = "/chat/once", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "챗봇 (단일 응답)")
    public Mono<ResponseEntity<RsData<String>>> chatOnce(@RequestBody @Validated AiChatReqBody req) {
        return Mono.fromCallable(() -> aiChatService.chatOnce(req))
                .subscribeOn(Schedulers.boundedElastic())
                .map(RsData::successOf)
                // ResponseEntity로 Content-Type JSON 명시하여 WebFlux 직렬화 문제 방지
                .map(rs -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(rs))
                .doOnError(e -> log.error("AI 챗봇 에러: ", e));
    }

    // 추후 챗봇 API를 통합하고, 내부 로직에서 RAG 기반 여부에 따라 분기 처리할 예정입니다.
    @PostMapping("/chat/rag")
    @Operation(summary = "RAG 기반 챗봇")
    public Mono<RsData<String>> chatWithRag(@RequestBody @Validated AiChatReqBody req) {
        return Mono.fromCallable(() -> aiChatService.chatWithRag(req.id(), req.message()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(RsData::successOf)
                .doOnError(e -> log.error("AI 챗봇 (RAG) 에러: ", e));
    }

    @PostMapping("/index")
    @Operation(summary = "블로그 벡터 DB 등록")
    public Mono<RsData<String>> indexBlog(@RequestBody AiIndexBlogReqBody req) {
        return Mono.fromRunnable(() -> aiIndexService.indexBlog(req.blogId(), req.title(), req.content()))
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(RsData.successOf("블로그 벡터 등록 완료")))
                .doOnError(e -> log.error("AI 블로그 벡터 DB 에러: ", e));
    }
}
