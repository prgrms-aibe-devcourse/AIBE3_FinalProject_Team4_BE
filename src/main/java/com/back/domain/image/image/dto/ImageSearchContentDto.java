package com.back.domain.image.image.dto;

public record ImageSearchContentDto(
        String url
) {
    public ImageSearchContentDto(UnsplashImageItem unsplashImageItem) {
        this(
                unsplashImageItem.urls().regular()
        );
    }

    public ImageSearchContentDto(GoogleImageItem googleImageItem) {
        this(
                googleImageItem.link()
        );
    }
}