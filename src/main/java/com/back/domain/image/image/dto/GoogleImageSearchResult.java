package com.back.domain.image.image.dto;

import java.util.List;

public record GoogleImageSearchResult(
        Queries queries,
        SearchInformation searchInformation,
        List<GoogleImageItem> items
) {
    public GoogleImageSearchResult withItems(List<GoogleImageItem> newItems) {
        return new GoogleImageSearchResult(
                this.queries,
                this.searchInformation,
                newItems
        );
    }
}

record Queries(
        List<Object> previousPage,
        List<Object> nextPage
) {
}

record SearchInformation(
        String totalResults
) {
}