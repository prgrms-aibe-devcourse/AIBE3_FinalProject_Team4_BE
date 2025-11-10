package com.back.domain.blog.blog.dto;

import com.back.domain.blog.blog.entity.BlogStatus;
import com.back.domain.hashtag.hashtag.dto.HashtagDto;

import java.time.LocalDateTime;
import java.util.List;

public record BlogWriteDto(
        Long id,
        String title,
        String content,
        List<String> hashTagNames,
        List<HashtagDto> hashtags,
        String thumbnailUrl,
        boolean isPublished,
        BlogStatus status,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {

}
