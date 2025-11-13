package com.back.domain.blog.blog.dto;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.bloghashtag.dto.BlogHashtagDto;
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
        List<BlogHashtagDto> hashtags,
        String status,
        Integer viewCount,
        Integer likeCount,
        Integer bookmarkCount,
        Boolean isLiked,
        Boolean isBookmarked,
        Integer commentCount,
        List<CommentResponseDto> comments,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer relatedShorlogCount,
        Long relatedShorlogId,
        Object[] sortValues
) {
    public BlogDto(Blog blog) {
        this(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getUser() != null ? new UserDto(blog.getUser()) : null,
                blog.getThumbnailUrl(),
                blog.getBlogHashtags().stream()
                        .filter(blogHashtag -> blogHashtag.getHashtag() != null)
                        .map(blogHashtag -> new BlogHashtagDto(blogHashtag.getHashtag())).toList(),
                blog.getStatus().name(),
                blog.getViewCount(),
                blog.getLikeCount(),
                blog.getBookmarkCount(),
//TODO: 댓글, 좋아요, 북마크 기능 리팩토링 후 수정
                null,
                null,
                blog.getCommentCount(),
                null,
                blog.getCreatedAt(),
                blog.getModifiedAt(),
                null,
                null,
                null
        );
    }
}
