package com.back.domain.search.search.service;

import com.back.domain.search.searchHistory.service.SearchHistoryService;
import com.back.domain.search.searchKeyword.service.SearchKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final SearchHistoryService searchHistoryService;
    private final SearchKeywordService searchKeywordService;

    public void search(Long userId, String keyword) {
        if(userId != null) {
            searchHistoryService.recordSearchHistory(userId, keyword);
        }

        searchKeywordService.incrementSearchCount(keyword);
    }
}
