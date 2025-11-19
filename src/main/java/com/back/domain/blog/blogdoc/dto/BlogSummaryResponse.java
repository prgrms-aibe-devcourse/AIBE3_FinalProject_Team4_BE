package com.back.domain.blog.blogdoc.dto;

import com.back.domain.blog.blogdoc.document.BlogDoc;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public record BlogSummaryResponse(
        Long id,
        Long userId,
        String userNickname,
        String title,
        String thumbnailUrl,
        long viewCount,
        long likeCount,
        long bookmarkCount,
        LocalDateTime createdAt
) {
    public BlogSummaryResponse(BlogDoc doc) {
        this(
                doc.getId(),
                doc.getUserId(),
                doc.getUserName(),
                doc.getTitle(),
                doc.getThumbnailUrl(),
                doc.getViewCount(),
                doc.getLikeCount(),
                doc.getBookmarkCount(),
                parseCreatedAt(doc.getCreatedAt())
        );
    }

    // ES에서 내려오는 "2025-11-18T16:34:15.449385Z" 파싱 함수
    public static LocalDateTime parseCreatedAt(String createdAt) {
        if (createdAt == null) return null;
        return OffsetDateTime.parse(createdAt)
                .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime();
    }
}
