package com.back.domain.ai.ai.dto;

import com.back.domain.ai.model.dto.AiModel;
import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(
        AiModel model,
        @NotBlank
        String message,
        Integer id,
        String content
) {
}
