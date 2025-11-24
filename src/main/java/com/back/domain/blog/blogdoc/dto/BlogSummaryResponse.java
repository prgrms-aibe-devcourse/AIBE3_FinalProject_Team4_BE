package com.back.domain.blog.blogdoc.dto;

import com.back.domain.blog.blogdoc.document.BlogDoc;
import com.back.domain.blog.blogdoc.util.EsDateTimeConverter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record BlogSummaryResponse(
        Long id,
        Long userId,
        String userNickname,
        String profileImageUrl,
        String title,
        String contentPre,
        String thumbnailUrl,
        List<String> hashtagNames,
        long viewCount,
        long likeCount,
        long bookmarkCount,
        long commentCount,
        boolean likedByMe,
        boolean bookmarkedByMe,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public BlogSummaryResponse(BlogDoc doc, Set<Long> likedIds, Set<Long> bookmarkedIds, Map<Long, Long> commentCounts) {
        this(
                doc.getId(),
                doc.getUserId(),
                doc.getUserNickname(),
                doc.getProfileImgUrl(),
                doc.getTitle(),
                generateContentPreview(doc.getContent()),
                doc.getThumbnailUrl(),
                doc.getHashtagName() != null ? doc.getHashtagName() : List.of(),
                doc.getViewCount(),
                doc.getLikeCount(),
                doc.getBookmarkCount(),
                commentCounts.getOrDefault(doc.getId(), 0L),
                likedIds.contains(doc.getId()),
                bookmarkedIds.contains(doc.getId()),
                EsDateTimeConverter.parseToKst(doc.getCreatedAt()),
                EsDateTimeConverter.parseToKst(doc.getModifiedAt())
        );
    }

    public static String generateContentPreview(String content) {
        if (content == null) return "";
        int previewLength = 80;
        return content.length() <= previewLength ? content : content.substring(0, previewLength) + "...";
    }
}
