package com.back.domain.search.searchHistory.dto;

import com.back.domain.search.searchHistory.entity.SearchHistory;

import java.time.LocalDateTime;

public record SearchHistoryResponseDto(
    Long id,
    String keyword,
    LocalDateTime createdAt
) {
    public SearchHistoryResponseDto(SearchHistory searchHistory) {
        this(
            searchHistory.getId(),
            searchHistory.getKeyword(),
            searchHistory.getCreatedAt()
        );
    }
}
