package com.back.domain.image.image.dto;

import java.util.List;

public record GoogleImageSearchResult(
        Queries queries,
        SearchInformation searchInformation,
        List<GoogleImageItem> items
) {
    public GoogleImageSearchResult() {
        this(new Queries(), new SearchInformation(), List.of());
    }

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
    Queries() {
        this(null, null);
    }
}

record SearchInformation(
        String totalResults
) {
    SearchInformation() {
        this("0");
    }
}