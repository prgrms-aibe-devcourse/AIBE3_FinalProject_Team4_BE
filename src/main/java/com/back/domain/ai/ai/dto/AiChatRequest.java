package com.back.domain.ai.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(
        String model,
        @NotBlank
        String message,
        Integer id,
        String content
) {
}
