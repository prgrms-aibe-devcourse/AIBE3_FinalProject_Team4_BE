package com.back.domain.blog.bookmark.dto;

public record BookmarkResponse(
        Long blogId,
        boolean bookmarked,
        long bookmarkCount
) {

}