package com.back.domain.blog.bookmark.dto;

public record BlogBookmarkResponse(
        Long blogId,
        boolean isBookmarked,
        long bookmarkCount
) {

}