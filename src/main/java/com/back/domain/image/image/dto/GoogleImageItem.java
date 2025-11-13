package com.back.domain.image.image.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GoogleImageItem {

    // 이미지 원본 URL (가장 중요)
    private String link;

    // 이미지 설명 (스니펫)
    private String snippet;

    @JsonProperty("image")
    private ImageDetail imageDetail; // 이미지 세부 정보 (높이, 너비 등)

    public static class ImageDetail {
        @JsonProperty("thumbnailLink")
        private String thumbnailLink; // 썸네일 이미지 URL

        public String getThumbnailLink() { return thumbnailLink; }
        // Setter 생략
    }

    // Getter and Setter (생략)
    public String getLink() { return link; }
    public String getSnippet() { return snippet; }
    public ImageDetail getImageDetail() { return imageDetail; }
}
