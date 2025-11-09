package com.back.domain.ai.ai.dto;

import jakarta.validation.constraints.NotNull;

public record AiAssistReqBody(
        @NotNull
        AiAssistMode mode,

        @NotNull
        AiAssistContentType contentType,

        String message,

        String content
) {
}
