package com.back.domain.search.search.service;

import com.back.domain.search.searchHistory.service.SearchHistoryService;
import com.back.domain.search.searchKeyword.service.SearchKeywordService;
import com.back.domain.shorlog.shorlog.dto.ShorlogFeedResponse;
import com.back.domain.shorlog.shorlog.service.ShorlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final SearchHistoryService searchHistoryService;
    private final SearchKeywordService searchKeywordService;
    private final ShorlogService shorlogService;


    public Page<ShorlogFeedResponse> search(Long userId, String keyword, String sort, int page) {

        if(userId != null) {
            searchHistoryService.recordSearchHistory(userId, keyword);
        }

        searchKeywordService.incrementSearchCount(keyword);

        return shorlogService.searchShorlogs(keyword, sort, page);
    }
}
