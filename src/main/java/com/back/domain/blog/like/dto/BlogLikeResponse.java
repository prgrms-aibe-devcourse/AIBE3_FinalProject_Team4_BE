package com.back.domain.blog.like.dto;

public record BlogLikeResponse(
        Long blogId,
        boolean isLiked,
        long likeCount
) {

}
