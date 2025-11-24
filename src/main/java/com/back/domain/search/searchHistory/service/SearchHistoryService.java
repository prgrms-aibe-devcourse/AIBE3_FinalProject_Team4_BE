package com.back.domain.search.searchHistory.service;

import com.back.domain.search.searchHistory.dto.SearchHistoryResponseDto;
import com.back.domain.search.searchHistory.entity.SearchHistory;
import com.back.domain.search.searchHistory.exception.SearchHistoryErrorCase;
import com.back.domain.search.searchHistory.repository.SearchHistoryRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.exception.UserErrorCase;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

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

    @Transactional
    public void recordSearchHistory(Long userId, String keyword) {
        Optional<SearchHistory> existingHistory = searchHistoryRepository.findByUserIdAndKeyword(userId, keyword);

        if(existingHistory.isPresent()) {
            SearchHistory history = existingHistory.get();
            history.updateTimestamp();
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));

        SearchHistory newHistory = new SearchHistory(keyword, user);
        searchHistoryRepository.save(newHistory);
    }
}
