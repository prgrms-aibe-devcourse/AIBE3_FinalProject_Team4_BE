package com.back.domain.blog.blogFile.dto;

import com.back.domain.blog.blogFile.entity.MediaKind;
import com.back.domain.shared.image.entity.Image;

public record BlogMediaUploadResponse(
        Long imageId,
        String url,
        MediaKind kind // IMAGE, VIDEO
) {
    public BlogMediaUploadResponse(Image image, MediaKind type) {
        this(
                image.getId(),
                image.getS3Url(),
                type
        );
    }
}
