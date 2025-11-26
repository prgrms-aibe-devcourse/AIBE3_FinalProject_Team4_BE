package com.back.domain.blog.blog.controller;

import com.back.domain.blog.blog.dto.*;
import com.back.domain.blog.blog.entity.BlogMySortType;
import com.back.domain.blog.blog.exception.BlogErrorCase;
import com.back.domain.blog.blog.service.BlogService;
import com.back.domain.blog.blogdoc.dto.BlogSliceResponse;
import com.back.global.config.security.SecurityUser;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.back.domain.recommend.recentview.constants.GuestConstants.GUEST_COOKIE_MAX_AGE;
import static com.back.domain.recommend.recentview.constants.GuestConstants.GUEST_COOKIE_NAME;

@RestController
@Tag(name = "Blog API", description = "블로그 기본 API")
@RequestMapping("api/v1/blogs")
@RequiredArgsConstructor
public class ApiV1BlogController {
    private final Rq rq;
    private final BlogService blogService;

    @PostMapping("")
    @Operation(summary = "블로그 글 작성")
    public RsData<BlogWriteDto> create(
            @Valid @RequestBody BlogWriteReqDto reqbody,
            @AuthenticationPrincipal SecurityUser userDetails
    ) {
        if (userDetails == null) {
            throw new ServiceException(BlogErrorCase.LOGIN_REQUIRED);
        }
        BlogWriteDto blogDto = blogService.write(userDetails.getId(), reqbody);

        return RsData.of("201-1", "블로그 글 작성이 완료되었습니다.", blogDto);
    }

    @GetMapping("/my")
    @Operation(summary = "내 블로그 글 다건 조회")
    public BlogSliceResponse<BlogDto> getMyItems(@AuthenticationPrincipal SecurityUser userDetails,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(defaultValue = "LATEST") BlogMySortType sortType) {
        if (userDetails == null) {
            throw new ServiceException(BlogErrorCase.LOGIN_REQUIRED);
        }
        PageRequest pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<BlogDto> result = blogService.findAllByMy(userDetails.getId(), sortType, pageable);
        boolean hasNext = result.hasNext();
        String nextCursor = hasNext ? String.valueOf(result.getNumber() + 1) : null;

        return new BlogSliceResponse<>(result.getContent(), hasNext, nextCursor
        );
    }

    @GetMapping("/bookmarks")
    @Operation(summary = "내가 북마크한 블로그 글 다건 조회")
    public BlogSliceResponse<BlogDto> getMyBookmarkedBlogs(
            @AuthenticationPrincipal SecurityUser userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "LATEST") BlogMySortType sortType
    ) {
        if (userDetails == null) {
            throw new ServiceException(BlogErrorCase.LOGIN_REQUIRED);
        }
        PageRequest pageable = PageRequest.of(page, size);
        Page<BlogDto> result = blogService.getMyBookmarkedBlogs(userDetails.getId(), sortType, pageable);
        boolean hasNext = result.hasNext();
        String nextCursor = hasNext ? String.valueOf(result.getNumber() + 1) : null;

        return new BlogSliceResponse<>(
                result.getContent(),
                hasNext,
                nextCursor
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "블로그 기본 글, 임시저장 글 단건 조회")
    public RsData<BlogDetailDto> getItem(@AuthenticationPrincipal SecurityUser userDetails, @PathVariable Long id) {
        Long userId = (userDetails != null) ? userDetails.getId() : null;
        BlogDetailDto blogdto = blogService.findById(userId, id);
        return RsData.of("200-2", "블로그 글 조회가 완료되었습니다.", blogdto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "블로그 글 수정, 임시저장 발행")
    public RsData<BlogWriteDto> modify(
            @PathVariable Long id,
            @Valid @RequestBody BlogWriteReqDto reqbody,
            @AuthenticationPrincipal SecurityUser userDetails
    ) {
        if (userDetails == null) {
            throw new ServiceException(BlogErrorCase.LOGIN_REQUIRED);
        }
        BlogWriteDto blogdto = blogService.modify(userDetails.getId(), id, reqbody);
        return RsData.of("200-3", "블로그 글 수정이 완료되었습니다.", blogdto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "블로그 기본 글, 임시저장 삭제")
    public RsData<Void> delete(@PathVariable Long id, @AuthenticationPrincipal SecurityUser userDetails) {
        if (userDetails == null) {
            throw new ServiceException(BlogErrorCase.LOGIN_REQUIRED);
        }
        blogService.delete(id, userDetails.getId());
        return new RsData<>("200-4", "블로그 글 삭제가 완료되었습니다.");
    }

    @PostMapping("/drafts")
    @Operation(summary = "블로그 임시저장 생성", description = "새 draft 생성, 응답으로 blogId 반환")
    public RsData<BlogWriteDto> saveDraft(
            @Valid @RequestBody BlogWriteReqDto reqbody,
            @AuthenticationPrincipal SecurityUser userDetails
    ) {
        if (userDetails == null) {
            throw new ServiceException(BlogErrorCase.LOGIN_REQUIRED);
        }
        BlogWriteDto blogDto = blogService.createDraft(userDetails.getId(), reqbody);
        return RsData.of("201-1", "블로그 임시저장이 완료되었습니다.", blogDto);
    }

    @PutMapping("/drafts/{blogId}")
    @Operation(summary = "블로그 임시저장 자동저장", description = "기존 draft 자동저장")
    public RsData<BlogWriteDto> updateDraft(
            @PathVariable Long blogId,
            @Valid @RequestBody BlogWriteReqDto reqbody,
            @AuthenticationPrincipal SecurityUser userDetails
    ) {
        if (userDetails == null) {
            throw new ServiceException(BlogErrorCase.LOGIN_REQUIRED);
        }
        BlogWriteDto blogDto = blogService.updateDraft(userDetails.getId(), blogId, reqbody);
        return RsData.of("200-3", "블로그 임시저장 글 업데이트가 완료되었습니다.", blogDto);
    }

    @GetMapping("/drafts")
    @Operation(summary = "블로그 임시저장 글 다건 조회")
    public RsData<List<BlogDraftDto>> getDrafts(@AuthenticationPrincipal SecurityUser userDetails) {
        if (userDetails == null) {
            throw new ServiceException(BlogErrorCase.LOGIN_REQUIRED);
        }
        List<BlogDraftDto> draftDtos = blogService.findDraftsByUserId(userDetails.getId());
        return RsData.of("200-2", "블로그 임시저장 글 조회가 완료되었습니다.", draftDtos);
    }

    @GetMapping("/{id}/view")
    @Operation(summary = "최근 본 블로그 추가")
    public RsData<Void> view(@CookieValue(value = GUEST_COOKIE_NAME, required = false) String guestId,
                                    @AuthenticationPrincipal SecurityUser securityUser,
                                    @PathVariable Long id) {
        if (id < 1) {
            throw new IllegalArgumentException("ID가 유효하지 않습니다.");
        }

        if (guestId == null) {
            guestId = UUID.randomUUID().toString();
            rq.setCookie(GUEST_COOKIE_NAME, guestId, GUEST_COOKIE_MAX_AGE);
        }

        Long userId = (securityUser == null) ? null : securityUser.getId();
        blogService.view(guestId, userId, id);

        return RsData.successOf(null);
    }
}
