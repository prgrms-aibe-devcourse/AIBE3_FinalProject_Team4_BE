package com.back.domain.image.image.service;

import com.back.domain.image.image.config.GoogleImageClient;
import com.back.domain.image.image.config.UnsplashImageClient;
import com.back.domain.image.image.dto.GoogleImageSearchResult;
import com.back.domain.image.image.dto.ImageSearchResBody;
import com.back.domain.image.image.dto.UnsplashImageSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final UnsplashImageClient unsplashImageClient;
    private final GoogleImageClient googleImageClient;

    public ImageSearchResBody getUnsplashImages(String query, int page, int size) {
        UnsplashImageSearchResult result = unsplashImageClient.searchImages(query, page + 1, size);

        return ImageSearchResBody.fromUnsplash(page, size, result);
    }

    public ImageSearchResBody getGoogleImages(String query, int page, int size) {
        GoogleImageSearchResult result = googleImageClient.searchImages(query, page + 1, size);

        return ImageSearchResBody.fromGoogle(page, size, result);
    }
}