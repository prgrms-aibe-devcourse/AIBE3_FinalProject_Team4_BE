package com.back.domain.blog.blog.dto;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.comments.comments.dto.CommentResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public record BlogDetailDto(
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
        long commentCount,
        Boolean isLiked,
        Boolean isBookmarked,
        List<CommentResponseDto> comments,// 기본과 다르게 댓글 포함
        Integer relatedShorlogCount,
        List<Long> relatedShorlogIds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public BlogDetailDto(Blog blog, boolean isBookmarked, boolean isLiked, List<CommentResponseDto> comments) {
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
                blog.getCommentCount(),
                //TODO: 댓글, 좋아요, 북마크 기능 리팩토링 후 수정
                isLiked,
                isBookmarked,
                comments,
                0,
                null,
                blog.getCreatedAt(),
                blog.getModifiedAt()
        );
    }

}

