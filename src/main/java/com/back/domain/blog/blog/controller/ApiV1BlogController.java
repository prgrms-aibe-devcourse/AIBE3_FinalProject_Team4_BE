package com.back.domain.blog.blog.controller;

import com.back.domain.blog.blog.dto.*;
import com.back.domain.blog.blog.entity.BlogMySortType;
import com.back.domain.blog.blog.service.BlogService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        BlogWriteDto blogDto = blogService.write(userDetails.getId(), reqbody);

        return RsData.of("201-1", "블로그 글 작성이 완료되었습니다.", blogDto);
    }

    @GetMapping("/my")
    @Operation(summary = "내 블로그 글 다건 조회")
    public RsData<List<BlogDto>> getMyItems(@AuthenticationPrincipal SecurityUser userDetails,
                                            @RequestParam(defaultValue = "LATEST") BlogMySortType sortType) {
        List<BlogDto> blogDtos = blogService.findAllByMy(userDetails.getId(), sortType);
        return RsData.of("200-1", "내 블로그 글 조회가 완료되었습니다.", blogDtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "블로그 기본 글, 임시저장 글 단건 조회")
    public RsData<BlogDetailDto> getItem(@AuthenticationPrincipal SecurityUser userDetails, @PathVariable Long id) {
        BlogDetailDto blogdto = blogService.findById(userDetails.getId(), id);
        return RsData.of("200-2", "블로그 글 조회가 완료되었습니다.", blogdto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "블로그 글 수정, 임시저장 발행")
    public RsData<BlogModifyDto> modify(
            @PathVariable Long id,
            @Valid @RequestBody BlogWriteReqDto reqbody,
            @AuthenticationPrincipal SecurityUser userDetails
    ) {
        BlogModifyDto blogdto = blogService.modify(userDetails.getId(), id, reqbody);
        return RsData.of("200-3", "블로그 글 수정이 완료되었습니다.", blogdto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "블로그 기본 글, 임시저장 삭제")
    public RsData<Void> delete(@PathVariable Long id, @AuthenticationPrincipal SecurityUser userDetails) {
        blogService.delete(id, userDetails.getId());
        return new RsData<>("200-4", "블로그 글 삭제가 완료되었습니다.");
    }

    @PostMapping("/drafts")
    @Operation(summary = "블로그 임시저장 생성")
    public RsData<BlogWriteDto> saveDraft(
            @Valid @RequestBody BlogWriteReqDto reqbody,
            @AuthenticationPrincipal SecurityUser userDetails
    ) {
        BlogWriteDto blogDto = blogService.createDraft(userDetails.getId(), reqbody);
        return RsData.of("201-1", "블로그 임시저장이 완료되었습니다.", blogDto);
    }

    @PutMapping("/drafts/{blogId}")
    @Operation(summary = "블로그 임시저장 자동저장")
    public RsData<BlogWriteDto> updateDraft(
            @PathVariable Long blogId,
            @Valid @RequestBody BlogWriteReqDto reqbody,
            @AuthenticationPrincipal SecurityUser userDetails
    ) {
        BlogWriteDto blogDto = blogService.updateDraft(userDetails.getId(), blogId, reqbody);
        return RsData.of("200-3", "블로그 임시저장 글 업데이트가 완료되었습니다.", blogDto);
    }

    @GetMapping("/drafts")
    @Operation(summary = "블로그 임시저장 글 다건 조회")
    public RsData<List<BlogDraftDto>> getDrafts(@AuthenticationPrincipal SecurityUser userDetails) {
        List<BlogDraftDto> draftDtos = blogService.findDraftsByUserId(userDetails.getId());
        return RsData.of("200-2", "블로그 임시저장 글 조회가 완료되었습니다.", draftDtos);
    }

    @GetMapping("/{id}/view")
    @Operation(summary = "최근 본 블로그")
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
