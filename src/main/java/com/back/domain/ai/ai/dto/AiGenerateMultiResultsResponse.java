package com.back.domain.ai.ai.dto;

import java.util.List;

public record AiGenerateMultiResultsResponse(
        List<String> results
) {
}
