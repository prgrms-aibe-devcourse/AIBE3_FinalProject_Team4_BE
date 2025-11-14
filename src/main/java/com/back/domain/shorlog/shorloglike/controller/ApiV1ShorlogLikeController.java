package com.back.domain.shorlog.shorloglike.controller;

import com.back.domain.shorlog.shorloglike.dto.ShorlogLikeResponse;
import com.back.domain.shorlog.shorloglike.service.ShorlogLikeService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Shorlog Like", description = "숏로그 좋아요 API")
@RestController
@RequestMapping("/api/v1/shorlog/{shorlogId}/like")
@RequiredArgsConstructor
public class ApiV1ShorlogLikeController {

    private final ShorlogLikeService shorlogLikeService;

    @PostMapping
    @Operation(summary = "좋아요 추가")
    public RsData<ShorlogLikeResponse> addLike(
            @PathVariable Long shorlogId,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        ShorlogLikeResponse response = shorlogLikeService.addLike(shorlogId, securityUser.getId());
        return RsData.successOf(response);
    }

    @DeleteMapping
    @Operation(summary = "좋아요 취소")
    public RsData<ShorlogLikeResponse> removeLike(
            @PathVariable Long shorlogId,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        ShorlogLikeResponse response = shorlogLikeService.removeLike(shorlogId, securityUser.getId());
        return RsData.successOf(response);
    }

    @GetMapping
    @Operation(summary = "좋아요 수 및 상태 조회")
    public RsData<ShorlogLikeResponse> getLikeStatus(
            @PathVariable Long shorlogId,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        // 비로그인 사용자도 조회 가능 (userId = null)
        Long userId = (securityUser != null) ? securityUser.getId() : null;
        ShorlogLikeResponse response = shorlogLikeService.getLikeStatus(shorlogId, userId);
        return RsData.successOf(response);
    }
}

