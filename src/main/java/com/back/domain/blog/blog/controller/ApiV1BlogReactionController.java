package com.back.domain.blog.blog.controller;

import com.back.domain.blog.blog.dto.ViewResponse;
import com.back.domain.blog.blog.service.BlogService;
import com.back.domain.blog.bookmark.dto.BlogBookmarkResponse;
import com.back.domain.blog.bookmark.service.BlogBookmarkService;
import com.back.domain.blog.like.dto.BlogLikeResponse;
import com.back.domain.blog.like.service.BlogLikeService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Blog Reaction API", description = "블로그 리액션 관련 API")
@RequestMapping("api/v1/blogs")
@RequiredArgsConstructor
public class ApiV1BlogReactionController {
    private final BlogService blogService;
    private final BlogLikeService blogLikeService;
    private final BlogBookmarkService blogBookmarkService;

    @PutMapping("/{id}/view")
    @Operation(summary = "블로그 글 조회수 증가")
    public RsData<ViewResponse> increaseView(@PathVariable Long id) {
        long viewCount = blogService.increaseView(id);
        return new RsData<>("200-2", "블로그 글 조회수가 증가되었습니다.", new ViewResponse(id, viewCount));
    }

    @PutMapping("/{id}/like")
    @Operation(summary = "블로그 글 좋아요 수 증가")
    public RsData<BlogLikeResponse> increaseLike(@PathVariable Long id, @AuthenticationPrincipal SecurityUser userDetails) {
        boolean on = blogLikeService.likeOn(userDetails.getId(), id);
        long likeCount = blogLikeService.getLikeCount(id);
        return new RsData<>("200-2", "블로그 글 좋아요 수가 증가되었습니다.", new BlogLikeResponse(id, on, likeCount));
    }

    @DeleteMapping("/{id}/like")
    @Operation(summary = "블로그 글 좋아요 수 감소")
    public RsData<BlogLikeResponse> decreaseLike(@PathVariable Long id, @AuthenticationPrincipal SecurityUser userDetails) {
        long likeCount = blogLikeService.likeOff(userDetails.getId(), id);
        return new RsData<>("200-2", "블로그 글 좋아요 수가 감소되었습니다.", new BlogLikeResponse(id, false, likeCount));
    }

    @PutMapping("/{id}/bookmark")
    @Operation(summary = "블로그 글 북마크 추가")
    public RsData<BlogBookmarkResponse> addBookmark(@PathVariable Long id, @AuthenticationPrincipal SecurityUser userDetails) {
        boolean on = blogBookmarkService.bookmarkOn(userDetails.getId(), id);
        long count = blogBookmarkService.getBookmarkCount(id);
        return new RsData<>("200-2", "블로그 글이 북마크에 추가되었습니다.", new BlogBookmarkResponse(id, on, count));
    }

    @DeleteMapping("/{id}/bookmark")
    @Operation(summary = "블로그 글 북마크 제거")
    public RsData<BlogBookmarkResponse> removeBookmark(@PathVariable Long id, @AuthenticationPrincipal SecurityUser userDetails) {
        long count = blogBookmarkService.bookmarkOff(userDetails.getId(), id);
        return new RsData<>("200-2", "블로그 글이 북마크에서 제거되었습니다.", new BlogBookmarkResponse(id, false, count));
    }
}
