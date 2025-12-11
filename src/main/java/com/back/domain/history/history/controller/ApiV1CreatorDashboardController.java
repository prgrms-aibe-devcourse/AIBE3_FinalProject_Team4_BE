package com.back.domain.history.history.controller;

import com.back.domain.blog.blog.exception.BlogErrorCase;
import com.back.domain.history.history.dto.CreatorOverviewDto;
import com.back.domain.history.history.service.CreatorDashboardService;
import com.back.global.config.security.SecurityUser;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/creator-dashboard")
@RequiredArgsConstructor
@Tag(name = "Creator Dashboard API", description = "크리에이터 통계 대시보드 API")
public class ApiV1CreatorDashboardController {
    private final CreatorDashboardService creatorDashboardService;

    @GetMapping("/overview")
    @Operation(summary = "크리에이터 개요 통계", description = "내 게시글/숏로그 기준 조회수/좋아요/북마크/팔로워 통계 요약")
    public RsData<CreatorOverviewDto> getOverview(@AuthenticationPrincipal SecurityUser user) {
        if (user == null) {
            throw new ServiceException(BlogErrorCase.LOGIN_REQUIRED);
        }

        CreatorOverviewDto dto = creatorDashboardService.getOverview(user.getId(), 7);
        return RsData.of("200-1", "크리에이터 대시보드 통계 조회가 완료되었습니다.", dto);
    }
}