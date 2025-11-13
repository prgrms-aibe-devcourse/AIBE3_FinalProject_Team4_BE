package com.back.domain.blog.blog.dto;

public record ViewResponse(
        Long blogId,
        Long viewCount
) {
}
