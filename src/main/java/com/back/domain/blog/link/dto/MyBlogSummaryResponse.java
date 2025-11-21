package com.back.domain.blog.link.dto;

import com.back.domain.blog.blog.entity.Blog;

import java.time.LocalDateTime;

public record MyBlogSummaryResponse(
        Long id,
        String content,
        LocalDateTime modifiedAt
) {
    public MyBlogSummaryResponse(Blog blog) {
        this(
                blog.getId(),
                blog.getContent(),
                blog.getModifiedAt()
        );
    }
}