package com.back.domain.ai.ai.dto;

import jakarta.validation.constraints.NotNull;

public record AiGenerateReqBody(
        String model,

        @NotNull
        AiGenerateMode mode,

        @NotNull
        AiGenerateContentType contentType,

        String message,

        String content
) {
}
