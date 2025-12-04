package com.back.domain.image.image.dto;

import java.util.List;

public record PixabayImageSearchResult(
        int totalHits,    // 검색어에 매칭된 이미지 수 (Pixabay가 리턴하는 값)
        List<PixabayImageItem> hits
) {
    public PixabayImageSearchResult() {
        this(0, List.of());
    }

    public PixabayImageSearchResult withItems(List<PixabayImageItem> newHits) {
        return new PixabayImageSearchResult(
                this.totalHits,
                newHits
        );
    }
}
