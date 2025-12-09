package com.back.domain.search.searchKeyword.service;

import com.back.domain.search.searchKeyword.dto.SearchKeywordResponseDto;
import com.back.domain.search.searchKeyword.entity.SearchKeyword;
import com.back.domain.search.searchKeyword.repository.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchKeywordService {
    private final SearchKeywordRepository searchKeywordRepository;

    @Transactional(readOnly = true)
    public List<SearchKeywordResponseDto> getTop10TrendingKeywords() {
        LocalDateTime from = LocalDateTime.now().minusDays(14);
        List<SearchKeyword> trendingKeywords = searchKeywordRepository.findTop10ByModifiedAtGreaterThanEqualOrderBySearchCountDesc(from);

        if (trendingKeywords.isEmpty()) {
            trendingKeywords =
                    searchKeywordRepository.findTop10ByOrderBySearchCountDesc();
        }

        return trendingKeywords.stream()
                .map(SearchKeywordResponseDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SearchKeywordResponseDto> getRecommendedKeywords(String keyword) {
        List<SearchKeyword> recommendedKeywords = searchKeywordRepository.findTop10ByKeywordContainingIgnoreCaseOrderBySearchCountDesc(keyword);

        return recommendedKeywords.stream()
                .map(SearchKeywordResponseDto::new)
                .toList();
    }

    @Transactional
    public void incrementSearchCount(String keyword) {
        SearchKeyword searchKeyword = searchKeywordRepository.findByKeyword(keyword)
                .orElseGet(() -> new SearchKeyword(keyword, 1L));

        searchKeyword.incrementSearchCount();
        searchKeywordRepository.save(searchKeyword);
    }

}
