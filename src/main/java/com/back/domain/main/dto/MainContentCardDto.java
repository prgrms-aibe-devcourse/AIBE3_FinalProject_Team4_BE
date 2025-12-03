package com.back.domain.main.dto;

import com.back.domain.main.entity.ContentType;
import lombok.Builder;

@Builder
public record MainContentCardDto(
        Long id,
        ContentType type,

        String title,          // Shorlog: content 앞부분
        String excerpt,        // 80자 미리보기
        String thumbnailUrl,   // 대표 이미지

        long likeCount,
        long bookmarkCount,
        long viewCount,

        String authorName,
        String createdAt,
        double score
) {}
