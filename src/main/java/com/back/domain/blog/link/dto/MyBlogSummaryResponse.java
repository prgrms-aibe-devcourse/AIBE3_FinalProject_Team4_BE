package com.back.domain.blog.link.dto;

import com.back.domain.blog.blog.entity.Blog;

import java.time.LocalDateTime;
import java.util.List;

public record MyBlogSummaryResponse(
        Long id,
        String content,
        List<String> hashtagNames,
        LocalDateTime modifiedAt
) {
    public MyBlogSummaryResponse(Blog blog) {
        this(
                blog.getId(),
                blog.getContent(),
                blog.getBlogHashtags().stream()
                        .map(blogHashtag -> blogHashtag.getHashtag().getName())
                        .toList(),
                blog.getModifiedAt()
        );
    }
}