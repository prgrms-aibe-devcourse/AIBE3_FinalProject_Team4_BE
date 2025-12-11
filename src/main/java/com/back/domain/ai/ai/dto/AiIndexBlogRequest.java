package com.back.domain.ai.ai.dto;

public record AiIndexBlogRequest(
        Integer blogId,
        String title,
        String content
) {
}
