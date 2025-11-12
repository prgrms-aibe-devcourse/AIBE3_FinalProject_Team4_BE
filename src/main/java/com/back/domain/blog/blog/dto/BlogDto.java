package com.back.domain.blog.blog.dto;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.hashtag.dto.BlogHashtagDto;

import java.time.LocalDateTime;
import java.util.List;

public record BlogDto(
        Long id,
        String title,
        String content,
//        UserDto author,
        String thumbnailUrl,
        List<BlogHashtagDto> hashtags,
        String status,
        Integer viewCount,
        Integer likeCount,
        Integer bookmarkCount,
        Boolean isLiked,
        Boolean isBookmarked,
        Integer commentCount,
//        List<CommentDto> comments,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer relatedShorlogCount,
        Object[] sortValues
) {
    public BlogDto(Blog blog) {
        this(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getThumbnailUrl(),
                blog.getBlogHashtags().stream()
                        .filter(blogHashtag -> blogHashtag.getHashtag() != null)
                        .map(blogHashtag -> new BlogHashtagDto(blogHashtag.getHashtag())).toList(),
                blog.getStatus().name(),
                blog.getViewCount(),
                blog.getLikeCount(),
                blog.getBookmarkCount(),
                null,
                null,
                blog.getCommentCount(),
//                blog.getComments().stream().map(CommentDto::new).toList(),
                blog.getCreatedAt(),
                blog.getModifiedAt(),
                null,
                null
        );
    }
}
