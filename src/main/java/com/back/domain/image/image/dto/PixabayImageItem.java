package com.back.domain.image.image.dto;

public record PixabayImageItem(
        long id,
        String pageURL,

        String webformatURL,
        int webformatWidth,
        int webformatHeight
) {
}
