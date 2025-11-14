package com.back.domain.blog.like.dto;

public record LikeResponse(
        Long blogId,
        boolean isLiked,
        long likeCount
) {

}
