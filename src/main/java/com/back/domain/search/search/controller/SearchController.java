package com.back.domain.search.search.controller;

import com.back.domain.search.search.service.SearchService;
import com.back.domain.shorlog.shorlog.dto.ShorlogFeedResponse;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "검색 API")
public class SearchController {
    private final SearchService searchService;

    @PostMapping
    @Operation(summary = "통합 검색 (숏로그)")
    public RsData<Void> search(@AuthenticationPrincipal SecurityUser user, @RequestParam String keyword) {
        Long userId = user != null ? user.getId() : null;
        searchService.search(userId, keyword);
        return RsData.successOf(null);
    }
}
