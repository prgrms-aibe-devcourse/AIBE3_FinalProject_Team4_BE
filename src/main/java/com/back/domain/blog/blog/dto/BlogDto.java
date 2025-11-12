package com.back.domain.blog.blog.dto;

import java.time.LocalDateTime;

public record BlogDto(
        Long id,
        String title,
        String content,
//        UserDto author,
        String thumbnailUrl,
//        List<BlogHashtag> hashtags,
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


}
