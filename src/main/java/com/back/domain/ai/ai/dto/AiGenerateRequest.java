package com.back.domain.ai.ai.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AiGenerateRequest(
        @NotNull
        AiGenerateMode mode,

        @NotNull
        AiGenerateContentType contentType,

        @Size(max = 1000)
        String message,

        String content,

        String[] previousResults
) {
}
