package com.back.domain.blog.blog.dto;

import com.back.domain.blog.blog.entity.BlogStatus;

public record BlogModifyDto(
        String title,
        String content,
        String summary,
        String thumbnailUrl,
        boolean isPublic,
        boolean allowComment,
        BlogStatus status
) {
}
