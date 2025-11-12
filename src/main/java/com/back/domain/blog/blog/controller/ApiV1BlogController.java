package com.back.domain.blog.blog.controller;

import com.back.domain.blog.blog.dto.BlogDraftDto;
import com.back.domain.blog.blog.dto.BlogDto;
import com.back.domain.blog.blog.dto.BlogWriteReqDto;
import com.back.domain.blog.blog.dto.ViewResponse;
import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.service.BlogService;
import com.back.domain.blog.bookmark.dto.BookmarkResponse;
import com.back.domain.blog.bookmark.service.BlogBookmarkService;
import com.back.domain.blog.like.dto.LikeResponse;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Blog", description = "블로그 API")
@RequestMapping("api/v1/blogs")
@RequiredArgsConstructor
public class ApiV1BlogController {
    private final BlogService blogService;
    private final BlogBookmarkService blogBookmarkService;

    @PostMapping("")
    @Operation(summary = "블로그 글 작성")
    public RsData<BlogDto> create(
            @Valid @RequestBody BlogWriteReqDto reqbody,
            @RequestParam(required = false) String thumbnailUrl,
            @AuthenticationPrincipal SecurityUser userDetails
    ) {
        Blog blog = blogService.write(userDetails.getId(), reqbody, thumbnailUrl);

        return new RsData<>("201-1", "블로그 글 작성이 완료되었습니다.", new BlogDto(blog));
    }

    @GetMapping("/{id}")
    @Operation(summary = "블로그 글 단건 조회")
    public RsData<BlogDto> getItem(@PathVariable Long id) {
        BlogDto blogdto = blogService.findById(id);
        return new RsData<>("200-2", "블로그 글 조회가 완료되었습니다.", blogdto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "블로그 글 수정")
    public RsData<BlogDto> modify(
            @PathVariable Long id,
            @Valid @RequestBody BlogWriteReqDto reqbody,
            @RequestParam(required = false) String thumbnailUrl,
            @AuthenticationPrincipal SecurityUser userDetails
    ) {
        BlogDto blogdto = blogService.modify(userDetails.getId(), id, reqbody, thumbnailUrl);
        return new RsData<>("200-3", "블로그 글 수정이 완료되었습니다.", blogdto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "블로그 글 삭제")
    public RsData<Void> delete(@PathVariable Long id) {
        blogService.delete(id);
        return new RsData<>("200-4", "블로그 글 삭제가 완료되었습니다.");
    }

    @PostMapping("/drafts/{blogId}")
    @Operation(summary = "블로그 임시저장")
    public RsData<BlogDto> saveDraft(
            @PathVariable Long blogId,
            @Valid @RequestBody BlogWriteReqDto reqbody,
            @RequestParam(required = false) String thumbnailUrl,
            @AuthenticationPrincipal SecurityUser userDetails
    ) {
        Blog blog = blogService.saveDraft(userDetails.getId(), blogId, reqbody, thumbnailUrl);
        return new RsData<>("201-1", "블로그 임시저장이 완료되었습니다.", new BlogDto(blog));
    }

    @GetMapping("/drafts")
    @Operation(summary = "블로그 임시저장 글 다건 조회")
    public RsData<List<BlogDraftDto>> getDrafts(@AuthenticationPrincipal SecurityUser userDetails) {
        List<BlogDraftDto> draftDtos = blogService.findDraftsByUserId(userDetails.getId());

        return new RsData<>("200-2", "블로그 임시저장 글 조회가 완료되었습니다.", draftDtos);
    }

    @PatchMapping("/{id}/view")
    @Operation(summary = "블로그 글 조회수 증가")
    public RsData<ViewResponse> increaseView(@PathVariable Long id) {
        long viewCount = blogService.increaseView(id);
        return new RsData<>("200-2", "블로그 글 조회수가 증가되었습니다.", new ViewResponse(id, viewCount));
    }

    @PatchMapping("/{id}/like")
    @Operation(summary = "블로그 글 좋아요 수 증가")
    public RsData<LikeResponse> increaseLike(@PathVariable Long id, @AuthenticationPrincipal SecurityUser userDetails) {
        long count = blogService.increaseLike(id);
        return new RsData<>("200-2", "블로그 글 좋아요 수가 증가되었습니다.");
    }

    @PatchMapping("/{id}/bookmark")
    @Operation(summary = "블로그 글 북마크 추가")
    public RsData<BookmarkResponse> addBookmark(@PathVariable Long id, @AuthenticationPrincipal SecurityUser userDetails) {
        boolean on = blogBookmarkService.bookmarkOn(userDetails.getId(), id);
        long count = blogBookmarkService.getBookmarkCount(id);
        return new RsData<>("200-2", "블로그 글이 북마크에 추가되었습니다.", new BookmarkResponse(id, on, count));
    }
}
