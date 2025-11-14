package com.back.domain.blog.blog.dto;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.comments.comments.dto.CommentResponseDto;
import com.back.domain.user.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

public record BlogDto(
        Long id,
        String title,
        String content,
        UserDto author,
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
        Integer relatedShorlogCount,
        List<Long> relatedShorlogIds,
        Object[] sortValues,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public BlogDto(Blog blog) {
        this(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getUser() != null ? new UserDto(blog.getUser()) : null,
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
                false,
                false,
                null,
                0,
                null,
                null,
                blog.getCreatedAt(),
                blog.getModifiedAt()
        );
    }
}
