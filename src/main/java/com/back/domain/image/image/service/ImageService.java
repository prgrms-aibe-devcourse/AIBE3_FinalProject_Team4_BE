package com.back.domain.image.image.service;

import com.back.domain.image.image.config.GoogleImageClient;
import com.back.domain.image.image.config.PixabayImageClient;
import com.back.domain.image.image.config.UnsplashImageClient;
import com.back.domain.image.image.dto.GoogleImageSearchResult;
import com.back.domain.image.image.dto.ImageSearchPagedResponse;
import com.back.domain.image.image.dto.PixabayImageSearchResult;
import com.back.domain.image.image.dto.UnsplashImageSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final UnsplashImageClient unsplashImageClient;
    private final GoogleImageClient googleImageClient;
    private final PixabayImageClient pixabayImageClient;

    public ImageSearchPagedResponse getUnsplashImages(String keyword, int number, int size) {
        UnsplashImageSearchResult result = unsplashImageClient.searchImages(keyword, number + 1, size);

        return ImageSearchPagedResponse.fromUnsplash(keyword, number, size, result);
    }

    public ImageSearchPagedResponse getGoogleImages(String keyword, int number, int size) {
        GoogleImageSearchResult result = googleImageClient.searchImages(keyword, number + 1, size);

        return ImageSearchPagedResponse.fromGoogle(keyword, number, size, result);
    }

    public ImageSearchPagedResponse getPixabayImages(String keyword, int number, int size) {
        PixabayImageSearchResult result = pixabayImageClient.searchImages(keyword, number + 1, size);

        return ImageSearchPagedResponse.fromPixabay(keyword, number, size, result);
    }
}