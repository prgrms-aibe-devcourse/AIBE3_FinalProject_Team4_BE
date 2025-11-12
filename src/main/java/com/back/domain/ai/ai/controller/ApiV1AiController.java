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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/v1/ais")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ApiV1AiController", description = "API AI 컨트롤러")
public class ApiV1AiController {
    private final AiGenerateService aiGenerateService;
    private final AiIndexService aiIndexService;
    private final AiChatService aiChatService;

    @PostMapping
    @Operation(summary = "블로그 제목 추천/해시태그 추천/블로그 내용 요약/키워드 추출")
    public Mono<RsData<Object>> generate(@RequestBody @Validated AiGenerateReqBody req) {
        return Mono.fromCallable(() -> aiGenerateService.generate(req))
                .subscribeOn(Schedulers.boundedElastic())
                .map(RsData::successOf);
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "챗봇")
    public Flux<RsData<String>> chat(@RequestBody @Validated AiChatReqBody req) {
        return aiChatService.chat(req)
                .map(RsData::successOf)
                .doOnCancel(() -> log.info("클라이언트가 AI 요청 중단"));
    }

    @PostMapping("/index")
    @Operation(summary = "블로그 벡터 색인")
    public Mono<RsData<String>> indexBlog(@RequestBody AiIndexBlogReqBody req) {
        return Mono.fromRunnable(() -> aiIndexService.indexBlog(req.id(), req.title(), req.content()))
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(RsData.successOf("블로그 벡터 등록 완료")));
    }

    @PostMapping("/chat/rag")
    @Operation(summary = "RAG 기반 챗봇")
    public Mono<RsData<String>> chatWithRag(@RequestBody @Validated AiChatReqBody req) {
        return Mono.fromCallable(() -> aiChatService.chatWithRag(req.id(), req.message()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(RsData::successOf);
    }
}
