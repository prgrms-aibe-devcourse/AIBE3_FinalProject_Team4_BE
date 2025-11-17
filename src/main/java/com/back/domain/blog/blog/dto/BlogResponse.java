package com.back.domain.blog.blog.dto;

public record BlogResponse(
        BlogDto blogBaseDto,
        boolean isBookmarked,
        boolean isLiked
) {
}
