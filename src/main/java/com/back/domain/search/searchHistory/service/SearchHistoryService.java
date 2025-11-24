package com.back.domain.search.searchHistory.service;

import com.back.domain.search.searchHistory.dto.SearchHistoryResponseDto;
import com.back.domain.search.searchHistory.entity.SearchHistory;
import com.back.domain.search.searchHistory.exception.SearchHistoryErrorCase;
import com.back.domain.search.searchHistory.repository.SearchHistoryRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {
    private final SearchHistoryRepository searchHistoryRepository;

    @Transactional(readOnly = true)
    public List<SearchHistoryResponseDto> getAllHistory(Long userId) {
        List<SearchHistory> histories = searchHistoryRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);

        return histories.stream()
                .map(SearchHistoryResponseDto::new)
                .toList();
    }

    @Transactional
    public void deleteSearchHistory(Long historyId, Long userId) {
        SearchHistory searchHistory = searchHistoryRepository.findById(historyId)
                .orElseThrow(() -> new ServiceException(SearchHistoryErrorCase.NOT_FOUND_SEARCH_HISTORY));

        if(!searchHistory.getUser().getId().equals(userId)) {
            throw new ServiceException(SearchHistoryErrorCase.PERMISSION_DENIED);
        }

        searchHistoryRepository.deleteById(historyId);
    }
}
