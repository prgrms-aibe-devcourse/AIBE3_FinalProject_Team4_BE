package com.back.domain.image.image.service;

import com.back.domain.image.image.UnsplashClient;
import com.back.domain.image.image.dto.UnsplashPhoto;
import com.back.domain.image.image.dto.UnsplashSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final UnsplashClient unsplashClient;

    public List<UnsplashPhoto> getImagesByKeyword(String query) {
        UnsplashSearchResult result = unsplashClient.searchImages(query);

        if (result != null && result.getResults() != null) {
            return result.getResults();
        }

        return List.of();
    }
}