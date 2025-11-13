package com.back.domain.image.image.service;

import com.back.domain.image.image.config.GoogleImageClient;
import com.back.domain.image.image.config.UnsplashImageClient;
import com.back.domain.image.image.dto.GoogleImageSearchResult;
import com.back.domain.image.image.dto.ImageSearchPagedResponse;
import com.back.domain.image.image.dto.UnsplashImageSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final UnsplashImageClient unsplashImageClient;
    private final GoogleImageClient googleImageClient;

    public ImageSearchPagedResponse getUnsplashImages(String query, int page, int size) {
        UnsplashImageSearchResult result = unsplashImageClient.searchImages(query, page + 1, size);

        return ImageSearchPagedResponse.fromUnsplash(page, size, result);
    }

    public ImageSearchPagedResponse getGoogleImages(String query, int page, int size) {
        GoogleImageSearchResult result = googleImageClient.searchImages(query, page + 1, size);

        return ImageSearchPagedResponse.fromGoogle(page, size, result);
    }
}