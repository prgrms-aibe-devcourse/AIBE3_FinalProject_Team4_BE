package com.back.domain.ai.ai.controller;

import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ais")
@RequiredArgsConstructor
@Slf4j
@Tag(name="ApiV1AiController", description = "API AI 컨트롤러")
public class ApiV1AiController {
    @Qualifier("openAiChatClient")
    private final ChatClient openAiChatClient;
    @Qualifier("huggingfaceChatClient")
    private final ChatClient huggingfaceChatClient;

    @GetMapping
    @Operation(summary = "ai api 연결 테스트")
    public RsData<String> generation(String userInput) {
        String result = this.openAiChatClient.prompt()
                .options(OpenAiChatOptions.builder().model("gpt-4o-mini").build())
                .user(userInput)
                .call()
                .content();

        return RsData.successOf(result);
    }
}
