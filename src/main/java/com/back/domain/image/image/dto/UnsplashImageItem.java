package com.back.domain.image.image.dto;

public record UnsplashImageItem(
        String id,
        Integer width,
        Integer height,
        Urls urls
) {
}

record Urls(
        String regular
) {
}