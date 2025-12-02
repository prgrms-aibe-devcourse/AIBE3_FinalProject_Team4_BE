package com.back.domain.image.image.dto;

public record ImageSearchContentDto(
        String url,
        Integer width,
        Integer height
) {
    public ImageSearchContentDto(UnsplashImageItem unsplashImageItem) {
        this(
                unsplashImageItem.urls().regular(),
                unsplashImageItem.width(),
                unsplashImageItem.height()
        );
    }

    public ImageSearchContentDto(GoogleImageItem googleImageItem) {
        this(
                googleImageItem.link(),
                googleImageItem.image().width(),
                googleImageItem.image().height()
        );
    }

    public ImageSearchContentDto(PixabayImageItem pixabayImageItem) {
        this(
                pixabayImageItem.webformatURL(),
                pixabayImageItem.webformatWidth(),
                pixabayImageItem.webformatHeight()
        );
    }
}