package com.back.domain.blog.blogdoc.controller;

import com.back.domain.blog.blogdoc.document.BlogSortType;
import com.back.domain.blog.blogdoc.dto.BlogSearchCondition;
import com.back.domain.blog.blogdoc.dto.BlogSliceResponse;
import com.back.domain.blog.blogdoc.dto.BlogSummaryResponse;
import com.back.domain.blog.blogdoc.service.BlogDocService;
import com.back.global.config.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/blogs")
@RestController
@RequiredArgsConstructor
@Validated
public class BlogDocController {
    private final BlogDocService blogDocService;

    @GetMapping
    @Operation(summary = "블로그 검색", description = "블로그 글 다건조회/검색/필터링 API")
    public BlogSliceResponse<BlogSummaryResponse> searchBlogs(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "LATEST") BlogSortType sort,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String cursor
    ) {
        BlogSearchCondition condition = new BlogSearchCondition(keyword, sort, size, cursor);
        return blogDocService.searchBlogs(condition);
    }
}
