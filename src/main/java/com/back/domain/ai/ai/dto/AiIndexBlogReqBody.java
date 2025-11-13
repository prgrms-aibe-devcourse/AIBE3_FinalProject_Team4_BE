package com.back.domain.ai.ai.dto;

public record AiIndexBlogReqBody(
        Integer blogId,
        String title,
        String content
) {
}
