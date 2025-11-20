package com.back.domain.blog.blogdoc.dto;

import com.back.domain.blog.blogdoc.document.BlogSortType;

public record BlogSearchCondition(
        String keyword,
        BlogSortType sortType,
        int size,
        String cursor
) {
}

