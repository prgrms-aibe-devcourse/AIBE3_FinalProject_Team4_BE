package com.back.domain.image.image.dto;

public record UnsplashImageItem(
        String id,
        Urls urls
) {
}

record Urls(
        String regular
) {
}