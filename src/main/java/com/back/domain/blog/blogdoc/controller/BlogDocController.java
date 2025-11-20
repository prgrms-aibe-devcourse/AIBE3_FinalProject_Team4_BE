package com.back.domain.blog.blogdoc.controller;

import com.back.domain.blog.blog.exception.BlogErrorCase;
import com.back.domain.blog.blogdoc.document.BlogScope;
import com.back.domain.blog.blogdoc.document.BlogSortType;
import com.back.domain.blog.blogdoc.dto.BlogSearchCondition;
import com.back.domain.blog.blogdoc.dto.BlogSliceResponse;
import com.back.domain.blog.blogdoc.dto.BlogSummaryResponse;
import com.back.domain.blog.blogdoc.service.BlogDocService;
import com.back.domain.user.follow.service.FollowService;
import com.back.global.config.security.SecurityUser;
import com.back.global.exception.ServiceException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/v1/blogs")
@RestController
@RequiredArgsConstructor
@Validated
public class BlogDocController {
    private final BlogDocService blogDocService;
    private final FollowService followService;

    @GetMapping
    @Operation(summary = "블로그 검색", description = "블로그 다건조회/검색/팔로우/정렬 필터링 API")
    public BlogSliceResponse<BlogSummaryResponse> searchBlogs(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "LATEST") BlogSortType sort,
            @RequestParam(defaultValue = "ALL") BlogScope scope,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String cursor
    ) {
        Long userId = (user != null) ? user.getId() : null;
        List<Long> authorIds = null;
        if (scope == BlogScope.FOLLOWING) {
            if (userId == null) {
                throw new ServiceException(BlogErrorCase.LOGIN_REQUIRED);
            }
            authorIds = followService.findFollowingUserIds(userId);
            if (authorIds.isEmpty()) {
                return new BlogSliceResponse<>(List.of(), false, null);
            }
        }
        BlogSearchCondition condition = new BlogSearchCondition(keyword, sort, size, cursor);
        return blogDocService.searchBlogs(user.getId(), condition, authorIds);
    }
}