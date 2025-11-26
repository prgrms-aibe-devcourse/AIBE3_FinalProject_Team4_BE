package com.back.domain.blog.blog.dto;

import com.back.domain.blog.blog.entity.Blog;

import java.time.LocalDateTime;
import java.util.List;

public record BlogDto(
        Long id,
        String title,
        String content,
        String username,
        String nickname,
        String thumbnailUrl,
        List<String> hashtagNames,
        String status,
        long viewCount,
        long likeCount,
        long bookmarkCount,
        boolean isLiked,
        boolean isBookmarked,
        long commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public BlogDto(Blog blog, boolean isLiked, boolean isBookmarked, long commentCount) {
        this(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getUser().getUsername(),
                blog.getUser().getNickname(),
                blog.getThumbnailUrl(),
                blog.getBlogHashtags().stream()
                        .map(blogHashtag -> blogHashtag.getHashtag().getName())
                        .toList(),
                blog.getStatus().name(),
                blog.getViewCount(),
                blog.getLikeCount(),
                blog.getBookmarkCount(),
                isLiked,
                isBookmarked,
                commentCount,
                blog.getCreatedAt(),
                blog.getModifiedAt()
        );
    }
}
