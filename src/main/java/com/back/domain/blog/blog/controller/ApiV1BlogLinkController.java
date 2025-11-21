package com.back.domain.blog.blog.controller;

import com.back.domain.blog.blog.dto.LinkShorlogReqDto;
import com.back.domain.blog.link.dto.BlogShorlogLinkResponse;
import com.back.domain.blog.link.dto.MyBlogSummaryResponse;
import com.back.domain.shared.link.service.ShorlogBlogLinkService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "BlogShorlogLink API", description = "블로그에서 숏로그 연결 API")
@RequestMapping("api/v1/blogs")
@RequiredArgsConstructor
public class ApiV1BlogLinkController {
    private final ShorlogBlogLinkService shorlogBlogLinkService;

    @PostMapping("/{blogId}/link-shorlog")
    @Operation(summary = "숏로그 연결")
    public RsData<BlogShorlogLinkResponse> linkShorlog(
            @PathVariable Long blogId,
            @RequestBody LinkShorlogReqDto req,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        BlogShorlogLinkResponse res = shorlogBlogLinkService.linkShorlog(blogId, req.shorlogId(), securityUser.getId());
        return RsData.successOf(res);
    }

    @DeleteMapping("/{id}/link-shorlog/{shorlogId}")
    @Operation(summary = "숏로그 연결 해제")
    public RsData<BlogShorlogLinkResponse> unlinkShorlog(
            @PathVariable Long id,
            @PathVariable Long shorlogId,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        BlogShorlogLinkResponse res = shorlogBlogLinkService.unlinkShorlog(id, shorlogId, securityUser.getId());
        return RsData.successOf(res);
    }

    @GetMapping("/my/recent")
    @Operation(summary = "내 최근 블로그 목록 조회")
    public RsData<List<MyBlogSummaryResponse>> getMyRecentBlogs(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(defaultValue = "7") int size
    ) {
        List<MyBlogSummaryResponse> list =
                shorlogBlogLinkService.getRecentBlogByAuthor(user.getId(), size);
        return RsData.successOf(list);
    }
}