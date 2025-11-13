package com.back.domain.ai.ai.dto;

import java.util.List;

public record AiGenerateKeywordResponse(
        List<String> keywords
) {
}
