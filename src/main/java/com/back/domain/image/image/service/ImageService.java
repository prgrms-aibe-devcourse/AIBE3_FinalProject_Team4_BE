package com.back.domain.image.image.service;

import com.back.domain.image.image.GoogleImageClient;
import com.back.domain.image.image.UnsplashImageClient;
import com.back.domain.image.image.dto.GoogleImageItem;
import com.back.domain.image.image.dto.GoogleSearchResponse;
import com.back.domain.image.image.dto.UnsplashPhoto;
import com.back.domain.image.image.dto.UnsplashSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final UnsplashImageClient unsplashImageClient;
    private final GoogleImageClient googleImageClient;

    public List<UnsplashPhoto> getUnsplashImages(String query) {
        UnsplashSearchResult result = unsplashImageClient.searchImages(query);

        if (result != null && result.getResults() != null) {
            return result.getResults();
        }

        return List.of();
    }

    public List<GoogleImageItem> getGoogleImages(String query) {

        GoogleSearchResponse result = googleImageClient.searchImages(query);

        if (result != null && result.getItems() != null) {
            return result.getItems(); // 검색 결과 목록 반환
        }

        return List.of();
    }
}