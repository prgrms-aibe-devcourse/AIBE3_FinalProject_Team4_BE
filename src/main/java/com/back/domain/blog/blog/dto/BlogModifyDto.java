package com.back.domain.blog.blog.dto;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogStatus;

import java.time.LocalDateTime;
import java.util.List;

public record BlogModifyDto(
        Long id,
        String title,
        String content,
        List<String> hashTagNames,
        String thumbnailUrl,
        boolean isPublished,
        BlogStatus status,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public BlogModifyDto(Blog blog) {
        this(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getBlogHashtags().stream()
                        .map(blogHashtag -> blogHashtag.getHashtag().getName())
                        .toList(),
                blog.getThumbnailUrl(),
                blog.getStatus() == BlogStatus.PUBLISHED,
                blog.getStatus(),
                blog.getCreatedAt(),
                blog.getModifiedAt()
        );
    }
}
