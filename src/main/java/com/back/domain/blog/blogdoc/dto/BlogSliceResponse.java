package com.back.domain.blog.blogdoc.dto;

import java.util.List;

public record BlogSliceResponse<T>(
        List<T> content,
        boolean hasNext,
        String nextCursor
) {
    public static <T> BlogSliceResponse<T> of(List<T> content, boolean hasNext, String nextCursor) {
        return new BlogSliceResponse<>(content, hasNext, nextCursor);
    }
}