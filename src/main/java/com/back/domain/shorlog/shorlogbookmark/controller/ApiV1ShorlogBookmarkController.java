package com.back.domain.shorlog.shorlogbookmark.controller;

import com.back.domain.shorlog.shorlogbookmark.dto.BookmarkListResponse;
import com.back.domain.shorlog.shorlogbookmark.dto.ShorlogBookmarkResponse;
import com.back.domain.shorlog.shorlogbookmark.service.ShorlogBookmarkService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Shorlog Bookmark", description = "숏로그 북마크 API")
@RestController
@RequestMapping("/api/v1/shorlog")
@RequiredArgsConstructor
public class ApiV1ShorlogBookmarkController {

    private final ShorlogBookmarkService shorlogBookmarkService;

    @PostMapping("/{shorlogId}/bookmark")
    @Operation(summary = "북마크 추가")
    public RsData<ShorlogBookmarkResponse> addBookmark(
            @PathVariable Long shorlogId,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        ShorlogBookmarkResponse response = shorlogBookmarkService.addBookmark(shorlogId, securityUser.getId());
        return RsData.successOf(response);
    }

    @DeleteMapping("/{shorlogId}/bookmark")
    @Operation(summary = "북마크 삭제")
    public RsData<ShorlogBookmarkResponse> removeBookmark(
            @PathVariable Long shorlogId,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        ShorlogBookmarkResponse response = shorlogBookmarkService.removeBookmark(shorlogId, securityUser.getId());
        return RsData.successOf(response);
    }

    @GetMapping("/bookmark")
    @Operation(summary = "내 북마크 목록 조회 (30개씩, 6열 격자형)")
    public RsData<BookmarkListResponse> getMyBookmarks(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page
    ) {
        BookmarkListResponse response = shorlogBookmarkService.getMyBookmarks(securityUser.getId(), sort, page);
        return RsData.successOf(response);
    }
}

