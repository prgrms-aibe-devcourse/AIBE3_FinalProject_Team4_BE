package com.back.domain.blog.blog.controller;

import com.back.domain.blog.blog.dto.ViewResponse;
import com.back.domain.blog.blog.service.BlogService;
import com.back.domain.blog.bookmark.dto.BookmarkResponse;
import com.back.domain.blog.bookmark.service.BlogBookmarkService;
import com.back.domain.blog.like.dto.LikeResponse;
import com.back.domain.blog.like.service.BlogLikeService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Blog", description = "블로그 reaction API")
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
    public RsData<LikeResponse> increaseLike(@PathVariable Long id, @AuthenticationPrincipal SecurityUser userDetails) {
        boolean on = blogLikeService.likeOn(userDetails.getUserId(), id);
        long likeCount = blogLikeService.getLikeCount(id);
        return new RsData<>("200-2", "블로그 글 좋아요 수가 증가되었습니다.", new LikeResponse(id, on, likeCount));
    }

    @DeleteMapping("/{id}/like")
    @Operation(summary = "블로그 글 좋아요 수 감소")
    public RsData<LikeResponse> decreaseLike(@PathVariable Long id, @AuthenticationPrincipal SecurityUser userDetails) {
        boolean off = blogLikeService.likeOff(userDetails.getUserId(), id);
        long likeCount = blogLikeService.getLikeCount(id);
        return new RsData<>("200-2", "블로그 글 좋아요 수가 감소되었습니다.", new LikeResponse(id, !off, likeCount));
    }

    @PutMapping("/{id}/bookmark")
    @Operation(summary = "블로그 글 북마크 추가")
    public RsData<BookmarkResponse> addBookmark(@PathVariable Long id, @AuthenticationPrincipal SecurityUser userDetails) {
        boolean on = blogBookmarkService.bookmarkOn(userDetails.getId(), id);
        long count = blogBookmarkService.getBookmarkCount(id);
        return new RsData<>("200-2", "블로그 글이 북마크에 추가되었습니다.", new BookmarkResponse(id, on, count));
    }

    @DeleteMapping("/{id}/bookmark")
    @Operation(summary = "블로그 글 북마크 제거")
    public RsData<BookmarkResponse> removeBookmark(@PathVariable Long id, @AuthenticationPrincipal SecurityUser userDetails) {
        boolean off = blogBookmarkService.bookmarkOff(userDetails.getId(), id);
        long count = blogBookmarkService.getBookmarkCount(id);
        return new RsData<>("200-2", "블로그 글이 북마크에서 제거되었습니다.", new BookmarkResponse(id, !off, count));
    }
}
