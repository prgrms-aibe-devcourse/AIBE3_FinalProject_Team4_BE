package com.back.domain.ai.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record AiChatReqBody(
        String model,
        @NotBlank
        String message,
        Integer id,
        String content
) {
}
