package com.back.domain.blog.blog.controller;

import com.back.domain.blog.blog.dto.*;
import com.back.domain.blog.blog.entity.BlogMySortType;
import com.back.domain.blog.blog.service.BlogService;
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
@Tag(name = "Blog API", description = "블로그 기본 API")
@RequestMapping("api/v1/blogs")
@RequiredArgsConstructor
public class ApiV1BlogController {
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
        BlogWriteDto blogdto = blogService.modify(userDetails.getId(), id, reqbody);
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
}
