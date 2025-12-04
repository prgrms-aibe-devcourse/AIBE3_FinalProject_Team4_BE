package com.back.domain.ai.ai.dto;

import com.back.domain.ai.model.dto.AiModel;
import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(
        Integer id,
        AiModel model,
        @NotBlank
        String message,
        String content
) {
}
