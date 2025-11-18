package com.back.domain.shorlog.shorlogimage.dto;

import com.back.domain.shared.image.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadImageResponse {
    private Long id;
    private String imageUrl;
    private String originalFilename;
    private Long fileSize;

    public static UploadImageResponse from(Image image) {
        return UploadImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getS3Url())  // S3 URL 직접 반환
                .originalFilename(image.getOriginalFilename())
                .fileSize(image.getFileSize())
                .build();
    }
}

