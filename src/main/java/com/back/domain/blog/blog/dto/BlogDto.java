package com.back.domain.blog.blog.dto;

import com.back.domain.blog.blogdoc.document.BlogDoc;
import com.back.domain.blog.hashtag.BlogHashtag;

import java.time.LocalDateTime;
import java.util.List;

public record BlogDto(
        Long id,
        String title,
        String content,
//        UserDto author,
        String thumbnailUrl,
        List<BlogHashtag> hashtags,
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
        Object[] sortValues
) {
    public BlogDto(BlogDoc blogDoc, Boolean isLiked, Boolean isBookmarked, Object[] sortValues) {
        this(
                blogDoc.getId() != null ? Long.parseLong(blogDoc.getId()) : null,
                blogDoc.getTitle(),
                blogDoc.getContent(),
//                new UserDto(blogDoc.getUserId(), blogDoc.getUserName(), blogDoc
//                        .getUserProfileImage()),
                blogDoc.getThumbnailImage(),
                blogDoc.getHashtags(),
                blogDoc.getStatus(),
                blogDoc.getViewCount(),
                blogDoc.getLikeCount(),
                blogDoc.getBookmarkCount(),
                isLiked,
                isBookmarked,
                blogDoc.getCommentCount(),
//                blogDoc.getComments().stream().map(CommentDto::new).toList(),
                blogDoc.getCreatedAt(),
                blogDoc.getUpdatedAt(),
                sortValues
        );
    }

}
