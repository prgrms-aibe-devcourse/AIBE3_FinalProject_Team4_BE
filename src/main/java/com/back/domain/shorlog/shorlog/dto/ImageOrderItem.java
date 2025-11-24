package com.back.domain.shorlog.shorlog.dto;

public record ImageOrderItem(
        int order,
        ImageOrderItemType type, // "FILE" or "URL"
        Integer fileIndex,       // "FILE"일 때만
        String url,              // "URL"일 때만
        String aspectRatio
) {
}

