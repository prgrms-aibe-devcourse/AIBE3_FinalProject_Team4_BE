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
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.back.domain.recommend.recommend.constants.GuestConstants.GUEST_COOKIE_NAME;

@RequestMapping("/api/v1/blogs")
@Tag(name = "Blog ES API", description = "블로그 검색/팔로우/정렬 필터링 API")
@RestController
@RequiredArgsConstructor
@Validated
public class BlogDocController {
    private final BlogDocService blogDocService;
    private final FollowService followService;

    @GetMapping
    @Operation(summary = "블로그 다건조회/검색", description = "블로그 다건조회/검색/팔로우/정렬 필터링 API")
    public BlogSliceResponse<BlogSummaryResponse> searchBlogs(
            @CookieValue(value = GUEST_COOKIE_NAME, required = false) String guestId, // 비로그인 개인화 추천용
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "LATEST") BlogSortType sort,
            @RequestParam(defaultValue = "ALL") BlogScope scope,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String cursor
    ) {
        Long userId = (user != null) ? user.getId() : null;
        List<Long> followingIds = null;
        if (scope == BlogScope.FOLLOWING) {
            if (userId == null) {
                throw new ServiceException(BlogErrorCase.LOGIN_REQUIRED);
            }
            followingIds = followService.findFollowingUserIds(userId);
            if (followingIds.isEmpty()) {
                return new BlogSliceResponse<>(List.of(), false, null);
            }
        }
        BlogSearchCondition condition = new BlogSearchCondition(keyword, sort, size, cursor);
        return blogDocService.searchBlogs(guestId, user.getId(), condition, followingIds);
    }
}