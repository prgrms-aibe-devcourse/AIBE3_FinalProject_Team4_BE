package com.back.domain.image.image.service;

import com.back.domain.image.image.config.GoogleImageClient;
import com.back.domain.image.image.config.UnsplashImageClient;
import com.back.domain.image.image.dto.GoogleImageSearchResult;
import com.back.domain.image.image.dto.UnsplashImageSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final UnsplashImageClient unsplashImageClient;
    private final GoogleImageClient googleImageClient;

    public UnsplashImageSearchResult getUnsplashImages(String query, int page, int size) {
        UnsplashImageSearchResult result = unsplashImageClient.searchImages(query, page, size);

        if (result != null && result.results() == null) {
            return result.withResults(List.of());
        }

        return result;
    }

    public GoogleImageSearchResult getGoogleImages(String query, int page, int size) {
        GoogleImageSearchResult result = googleImageClient.searchImages(query, page, size);

        if (result != null && result.items() == null) {
            return result.withItems(List.of());
        }

        return result;
    }
}