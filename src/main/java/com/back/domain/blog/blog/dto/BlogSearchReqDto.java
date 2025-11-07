package com.back.domain.blog.blog.dto;

import lombok.Builder;

import java.util.List;


@Builder
public record BlogSearchReqDto(
        String keyword,
        Long categoryId,
        List<String> hashtags,
        Boolean followingOnly,
        String sortType, // latest, oldest, views,
        List<Object> searchAfter,
        Integer size,
        Long currentUserId
) {
}

