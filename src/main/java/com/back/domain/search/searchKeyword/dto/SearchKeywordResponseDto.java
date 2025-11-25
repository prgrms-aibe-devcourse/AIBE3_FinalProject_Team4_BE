package com.back.domain.search.searchKeyword.dto;

import com.back.domain.search.searchKeyword.entity.SearchKeyword;

public record SearchKeywordResponseDto(
        String keyword,
        Long searchCount
) {
    public SearchKeywordResponseDto(SearchKeyword searchKeyword) {
        this(
                searchKeyword.getKeyword(),
                searchKeyword.getSearchCount()
        );
    }
}
