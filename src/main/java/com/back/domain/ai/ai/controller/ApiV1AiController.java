package com.back.domain.ai.ai.controller;

import com.back.domain.ai.ai.dto.AiChatRequest;
import com.back.domain.ai.ai.dto.AiGenerateRequest;
import com.back.domain.ai.ai.dto.AiIndexBlogRequest;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public RsData<Object> generate(@RequestBody @Validated AiGenerateRequest req) {
        return RsData.successOf(aiGenerateService.generate(req));
    }

    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "챗봇 (단일 응답)")
    public RsData<String> chatOnce(@RequestBody @Validated AiChatRequest req) {
        return RsData.successOf(aiChatService.chatOnce(req));
    }

    // 추후 챗봇 API를 통합하고, 내부 로직에서 RAG 기반 여부에 따라 분기 처리할 예정입니다.
    @PostMapping("/chat/rag")
    @Operation(summary = "RAG 기반 챗봇")
    public RsData<String> chatWithRag(@RequestBody @Validated AiChatRequest req) {
        return RsData.successOf(aiChatService.chatWithRag(req.id(), req.message()));
    }

    @PostMapping("/index")
    @Operation(summary = "블로그 벡터 DB 등록")
    public RsData indexBlog(@RequestBody AiIndexBlogRequest req) {
        aiIndexService.indexBlog(req.blogId(), req.title(), req.content());
        return new RsData<>("201-1", "벡터 DB 등록이 완료되었습니다.");
    }
}
