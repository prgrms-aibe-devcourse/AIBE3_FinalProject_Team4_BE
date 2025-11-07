package com.back.domain.blog.blog.dto;

import java.util.List;

public record BlogSearchDto(
        List<BlogDto> blogs,
        Object[] nextCursor,
        boolean hasNext
) {
}
