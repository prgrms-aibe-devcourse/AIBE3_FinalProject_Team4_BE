package com.back.domain.ai.ai.controller;

import com.back.domain.ai.ai.dto.AiAssistReqBody;
import com.back.domain.ai.ai.service.AiService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ais")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ApiV1AiController", description = "API AI 컨트롤러")
public class ApiV1AiController {
    private final AiService aiService;

    @PostMapping
    @Operation(summary = "제목 추천")
    public RsData<?> assist(@RequestBody @Validated AiAssistReqBody req) {
        return RsData.successOf(aiService.generate(req));
    }
}
