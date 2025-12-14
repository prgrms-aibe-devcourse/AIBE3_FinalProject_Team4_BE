package com.back.domain.ai.ai.dto;

import com.back.domain.ai.model.dto.AiModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiChatRequest(
        Integer id,
        AiModel model,
        @NotBlank
        @Size(max = 1000)
        String message,
        String content
) {
}
