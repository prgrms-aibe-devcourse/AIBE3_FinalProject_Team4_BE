package com.back.domain.search.searchKeyword.controller;

import com.back.domain.search.searchKeyword.dto.SearchKeywordResponseDto;
import com.back.domain.search.searchKeyword.service.SearchKeywordService;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchKeywordController {
    private final SearchKeywordService searchKeywordService;

    @GetMapping("/trends")
    public RsData<List<SearchKeywordResponseDto>> getTop10TrendingKeywords() {
        List<SearchKeywordResponseDto> dtos = searchKeywordService.getTop10TrendingKeywords();
        return RsData.of("200", "인기 검색어 조회 성공", dtos);
    }

    @GetMapping("/recommendations/{keyword}")
    public RsData<List<SearchKeywordResponseDto>> getRecommendedKeywords(@Valid @PathVariable String keyword) {
        List<SearchKeywordResponseDto> dtos = searchKeywordService.getRecommendedKeywords(keyword);
        return RsData.of("200", "추천 검색어 조회 성공", dtos);
    }
}
