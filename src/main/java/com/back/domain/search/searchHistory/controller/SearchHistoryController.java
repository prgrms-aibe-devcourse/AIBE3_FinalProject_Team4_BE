package com.back.domain.search.searchHistory.controller;

import com.back.domain.search.searchHistory.dto.SearchHistoryResponseDto;
import com.back.domain.search.searchHistory.service.SearchHistoryService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search/history")
public class SearchHistoryController {
    private final SearchHistoryService searchHistoryService;

    @GetMapping
    public RsData<List<SearchHistoryResponseDto>> getAllHistory(@AuthenticationPrincipal SecurityUser user) {
        List<SearchHistoryResponseDto> dtos = searchHistoryService.getAllHistory(user.getId());
        return RsData.of("200", "검색 기록 조회 성공", dtos);
    }

    @DeleteMapping("/{historyId}")
    public RsData<Void> deleteSearchHistory(@Valid @PathVariable Long historyId, @AuthenticationPrincipal SecurityUser user) {
        searchHistoryService.deleteSearchHistory(historyId, user.getId());
        return RsData.of("200", "검색 기록 삭제 성공", null);
    }

}
