package com.back.domain.ai.ai.dto;

public record BlogIndexingReqBody(
        String title,
        String content,
        Integer blogId
) {
}
