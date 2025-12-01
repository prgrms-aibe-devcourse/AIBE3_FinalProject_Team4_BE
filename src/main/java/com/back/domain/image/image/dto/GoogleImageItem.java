package com.back.domain.image.image.dto;

public record GoogleImageItem(
        String link,
        String displayLink, // 이미지 출처를 보여주는 링크 (도메인)
        Image image
) {
}
record Image(
        Integer width,
        Integer height
) {
}