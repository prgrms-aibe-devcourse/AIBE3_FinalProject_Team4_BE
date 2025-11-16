package com.back.domain.ai.ai.dto;

import jakarta.validation.constraints.NotNull;

public record AiGenerateRequest(
        @NotNull
        AiGenerateMode mode,

        @NotNull
        AiGenerateContentType contentType,

        String message,

        String content
) {
}
