package com.back.domain.blog.blog.dto;

import com.back.domain.blog.blog.entity.Blog;

import java.time.LocalDateTime;
import java.util.List;

public record BlogWriteDto(
        Long id,
        String title,
        String content,
        Long userId,
        String thumbnailUrl,
        List<String> hashtagNames,
        String status,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {

    public BlogWriteDto(Blog blog) {
        this(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getUser().getId(),
                blog.getThumbnailUrl(),
                blog.getBlogHashtags().stream()
                        .map(blogHashtag -> blogHashtag.getHashtag().getName())
                        .toList(),
                blog.getStatus().name(),
                blog.getCreatedAt(),
                blog.getModifiedAt()
        );
    }
}
