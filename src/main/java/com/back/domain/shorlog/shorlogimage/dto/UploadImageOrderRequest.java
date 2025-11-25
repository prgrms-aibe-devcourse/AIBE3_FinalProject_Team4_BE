package com.back.domain.shorlog.shorlogimage.dto;

public record UploadImageOrderRequest(
        int order,
        ImageOrderItemType type, // "FILE" or "URL"
        Integer fileIndex,       // "FILE"일 때만
        String url,              // "URL"일 때만
        String aspectRatio
) {
}

