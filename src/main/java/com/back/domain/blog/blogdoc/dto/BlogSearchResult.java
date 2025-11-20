package com.back.domain.blog.blogdoc.dto;

import com.back.domain.blog.blogdoc.document.BlogDoc;

import java.util.List;

public record BlogSearchResult(
        List<BlogDoc> docs,
        boolean hasNext,
        String nextCursor
) {
}