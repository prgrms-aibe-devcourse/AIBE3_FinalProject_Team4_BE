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
        List<CommentResponseDto> comments,// 기본과 다르게 댓글 포함
        Integer relatedShorlogCount,
        List<Long> relatedShorlogIds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public BlogDetailDto(Blog blog, boolean isBookmarked, boolean isLiked, List<CommentResponseDto> comments, long commentCount) {
        this(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getUser().getUsername(),
                blog.getUser().getNickname(),
                blog.getUser().getProfileImgUrl(),
                blog.getThumbnailUrl(),
                blog.getBlogHashtags().stream()
                        .map(blogHashtag -> blogHashtag.getHashtag().getName())
                        .toList(),
                blog.getStatus().name(),
                blog.getViewCount(),
                blog.getLikeCount(),
                blog.getBookmarkCount(),
                commentCount,
                //TODO: 연결 관련추가 후 수정
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

