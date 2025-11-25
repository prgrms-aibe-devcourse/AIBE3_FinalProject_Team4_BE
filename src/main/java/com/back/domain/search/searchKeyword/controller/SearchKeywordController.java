package com.back.domain.search.searchKeyword.controller;

import com.back.domain.search.searchKeyword.dto.SearchKeywordResponseDto;
import com.back.domain.search.searchKeyword.service.SearchKeywordService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search/trends")
@Tag(name = "Search Keyword", description = "검색 키워드 API")
public class SearchKeywordController {
    private final SearchKeywordService searchKeywordService;

    @GetMapping("/top10")
    @Operation(summary = "상위 10개 인기 검색어 조회")
    public RsData<List<SearchKeywordResponseDto>> getTop10TrendingKeywords() {
        List<SearchKeywordResponseDto> dtos = searchKeywordService.getTop10TrendingKeywords();
        return RsData.of("200", "인기 검색어 조회 성공", dtos);
    }

    @GetMapping("/recommend")
    @Operation(summary = "키워드 기반 추천 검색어 조회")
    public RsData<List<SearchKeywordResponseDto>> getRecommendedKeywords(@Valid @RequestParam String keyword) {
        List<SearchKeywordResponseDto> dtos = searchKeywordService.getRecommendedKeywords(keyword);
        return RsData.of("200", "추천 검색어 조회 성공", dtos);
    }
}
