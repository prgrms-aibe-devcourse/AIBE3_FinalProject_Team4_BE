package com.back.domain.image.image.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record UnsplashImageSearchResult(
        int total,

        @JsonProperty("total_pages")
        int totalPages,

        List<UnsplashImageItem> results
) {
    public UnsplashImageSearchResult withResults(List<UnsplashImageItem> newResults) {
        return new UnsplashImageSearchResult(
                this.total,
                this.totalPages,
                newResults
        );
    }
}
