package com.back.domain.blog.blog.controller;

import com.back.domain.blog.blog.dto.LinkShorlogReqDto;
import com.back.domain.shared.link.service.ShorlogBlogLinkService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Blog Link API", description = "블로그에서 쇼로그 연결 API")
@RequestMapping("api/v1/blogs")
@RequiredArgsConstructor
public class ApiV1BlogLinkController {
    private final ShorlogBlogLinkService shorlogBlogLinkService;

    @PostMapping("/{id}/link-shorlog")
    @Operation(summary = "쇼로그 연결")
    public RsData<Void> linkShorlog(
            @PathVariable Long id,
            @RequestBody LinkShorlogReqDto req,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        shorlogBlogLinkService.linkShorlog(id, req.shorlogId(), securityUser.getId());
        return RsData.successOf(null);
    }

    @DeleteMapping("/{id}/link-shorlog/{shorlogId}")
    @Operation(summary = "쇼로그 연결 해제")
    public RsData<Void> unlinkShorlog(
            @PathVariable Long id,
            @PathVariable Long shorlogId,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        shorlogBlogLinkService.unlinkShorlog(id, shorlogId, securityUser.getId());
        return RsData.successOf(null);
    }
}
