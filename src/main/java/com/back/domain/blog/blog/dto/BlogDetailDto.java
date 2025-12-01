package com.back.domain.blog.blog.dto;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blogFile.dto.BlogFileDto;
import com.back.domain.blog.blogFile.entity.BlogFile;
import com.back.domain.comments.comments.dto.CommentResponseDto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record BlogDetailDto(
        Long id,
        String title,
        String content,
        Long userId,
        String username,
        String nickname,
        String profileImageUrl,
        String thumbnailUrl,
        List<String> hashtagNames,
        String status,
        long viewCount,
        long likeCount,
        long bookmarkCount,
        long commentCount,
        Boolean isLiked,
        Boolean isBookmarked,
        List<CommentResponseDto> comments,
        List<BlogFileDto> images,
        long linkedShorlogCount,
        boolean hasLinkedShorlogs,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public BlogDetailDto(Blog blog, List<String> hashtagNames, boolean isBookmarked, boolean isLiked, List<CommentResponseDto> comments, long commentCount, long linkedShorlogCount) {
        this(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getUser().getId(),
                blog.getUser().getUsername(),
                blog.getUser().getNickname(),
                blog.getUser().getProfileImgUrl(),
                blog.getThumbnailUrl(),
                hashtagNames,
                blog.getStatus().name(),
                blog.getViewCount(),
                blog.getLikeCount(),
                blog.getBookmarkCount(),
                commentCount,
                isLiked,
                isBookmarked,
                comments,
                blog.getBlogFiles().stream()
                        .sorted(Comparator.comparing(BlogFile::getSortOrder)).map(BlogFileDto::new).toList(),
                linkedShorlogCount,
                linkedShorlogCount > 0,
                blog.getCreatedAt(),
                blog.getModifiedAt()
        );
    }

}

