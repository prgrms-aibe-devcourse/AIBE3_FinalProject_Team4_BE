package com.back.domain.shorlog.shorlogbloglink.controller;

import com.back.domain.shared.link.dto.LinkBlogRequest;
import com.back.domain.shared.link.service.ShorlogBlogLinkService;
import com.back.domain.shorlog.shorlogbloglink.dto.MyShorlogSummaryResponse;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "ShorlogBlogLink", description = "숏로그-블로그 연결 API")
@RestController
@RequestMapping("/api/v1/shorlog")
@RequiredArgsConstructor
public class ApiV1ShorlogBlogLinkController {

    private final ShorlogBlogLinkService shorlogBlogLinkService;

    @PostMapping("/{id}/link-blog")
    @Operation(summary = "블로그 연결")
    public RsData<Void> linkBlog(
            @PathVariable Long id,
            @RequestBody LinkBlogRequest request,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        shorlogBlogLinkService.linkBlog(id, request.blogId(), securityUser.getId());
        return RsData.successOf(null);
    }

    @DeleteMapping("/{id}/link-blog/{blogId}")
    @Operation(summary = "블로그 연결 해제")
    public RsData<Void> unlinkBlog(
            @PathVariable Long id,
            @PathVariable Long blogId,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        shorlogBlogLinkService.unlinkBlog(id, blogId, securityUser.getId());
        return RsData.successOf(null);
    }

    @GetMapping("/my/recent-shorlogs")
    @Operation(summary = "연결할 내 최근 숏로그 목록 조회")
    public RsData<List<MyShorlogSummaryResponse>> getMyRecentShorlogs(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(defaultValue = "7") int size
    ) {
        List<MyShorlogSummaryResponse> list =
                shorlogBlogLinkService.getRecentShorlogByAuthor(user.getId(), size);
        return RsData.successOf(list);
    }
}

