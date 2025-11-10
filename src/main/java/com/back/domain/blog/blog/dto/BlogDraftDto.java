package com.back.domain.blog.blog.dto;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogStatus;

import java.time.LocalDateTime;

public record BlogDraftDto(
        String title,
        String content,
        String thumbnailUrl,
        boolean isPublic,
        BlogStatus status,
        LocalDateTime modifiedAt
) {
    public BlogDraftDto(Blog blog) {
        this(
                blog.getTitle(),
                blog.getContent(),
                blog.getThumbnailUrl(),
                blog.isPublished(),
                blog.getStatus(),
                blog.getModifiedAt()
        );
    }
}
