package com.back.domain.ai.ai.controller;

import com.back.domain.ai.ai.dto.AiAssistReqBody;
import com.back.domain.ai.ai.dto.BlogIndexingReqBody;
import com.back.domain.ai.ai.service.AiService;
import com.back.domain.ai.ai.service.BlogChatService;
import com.back.domain.ai.ai.service.BlogIndexingService;
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

@RestController
@RequestMapping("/api/v1/ais")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ApiV1AiController", description = "API AI 컨트롤러")
public class ApiV1AiController {
    private final AiService aiService;
    private final BlogIndexingService blogIndexingService;
    private final BlogChatService blogChatService;

    @PostMapping
    @Operation(summary = "제목 추천/해시태그 추천/내용 요약/키워드 추출")
    public Mono<RsData<Object>> assist(@RequestBody @Validated AiAssistReqBody req) {
        return aiService.generate(req)
                .map(RsData::<Object>successOf)
                .onErrorResume(e ->
                        Mono.just(RsData.of("500-1", "AI_ERROR", null))
                );
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "채팅")
    public Flux<RsData<String>> chat(@RequestBody @Validated AiAssistReqBody req) {
        return aiService.chat(req)
                .map(RsData::successOf)
                .doOnCancel(() -> log.info("클라이언트가 AI 요청 중단"));
    }

    // 블로그 색인 요청 (실제 데이터 파이프라인에서 호출)
    @PostMapping("/api/v1/index")
    public String indexBlog(@RequestBody BlogIndexingReqBody req) {
        // "/save"
        // saveBlog
//        return Mono.fromRunnable(() -> blogIndexingService.indexBlogContent(req.content(), req.title(), req.blogId()))
//                .thenReturn("블로그 저장 및 벡터 등록 완료");
        blogIndexingService.indexBlogContent(req.content(), req.title(), req.blogId());
        return "블로그 내용 색인(Indexing) 완료!";
    }
    // 사용자 채팅 요청
    @GetMapping("/api/v1/index/chat")
    public String ragchat(@RequestParam String q, @RequestParam Integer blogId) {
        // return Mono.fromSupplier(() -> ragService.askWithContext(question));
        return blogChatService.ragChat(q, blogId);
    }
}
