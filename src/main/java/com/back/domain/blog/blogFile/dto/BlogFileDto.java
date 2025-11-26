package com.back.domain.blog.blogFile.dto;

import com.back.domain.blog.blogFile.entity.BlogFile;

public record BlogFileDto(
        Long imageId,
        String url,
        Integer sortOrder,
        String contentType
) {
    public BlogFileDto(BlogFile blogFile) {
        this(
                blogFile.getImage().getId(),
                blogFile.getImage().getS3Url(),
                blogFile.getSortOrder(),
                blogFile.getImage().getContentType()
        );
    }
}