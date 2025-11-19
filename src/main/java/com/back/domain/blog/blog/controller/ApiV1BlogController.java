package com.back.domain.blog.blog.controller;

import com.back.domain.blog.blog.dto.*;
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
@Tag(name = "Blog", description = "블로그 API")
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

    @GetMapping("/{id}")
    @Operation(summary = "블로그 글 단건 조회")
    public RsData<BlogDetailDto> getItem(@AuthenticationPrincipal SecurityUser userDetails, @PathVariable Long id) {
        BlogDetailDto blogdto = blogService.findById(userDetails.getId(), id);
        return RsData.of("200-2", "블로그 글 조회가 완료되었습니다.", blogdto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "블로그 글 수정")
    public RsData<BlogModifyDto> modify(
            @PathVariable Long id,
            @Valid @RequestBody BlogWriteReqDto reqbody,
            @AuthenticationPrincipal SecurityUser userDetails
    ) {
        BlogModifyDto blogdto = blogService.modify(userDetails.getId(), id, reqbody);
        return RsData.of("200-3", "블로그 글 수정이 완료되었습니다.", blogdto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "블로그 글 삭제")
    public RsData<Void> delete(@PathVariable Long id, @AuthenticationPrincipal SecurityUser userDetails) {
        blogService.delete(id, userDetails.getId());
        return new RsData<>("200-4", "블로그 글 삭제가 완료되었습니다.");
    }

    @PostMapping("/drafts")
    @Operation(summary = "블로그 임시저장")
    public RsData<BlogWriteDto> saveDraft(
            @Valid @RequestBody BlogWriteReqDto reqbody,
            @AuthenticationPrincipal SecurityUser userDetails
    ) {
        BlogWriteDto blogDto = blogService.saveDraft(userDetails.getId(), reqbody);
        return RsData.of("201-1", "블로그 임시저장이 완료되었습니다.", blogDto);
    }

    @GetMapping("/drafts")
    @Operation(summary = "블로그 임시저장 글 다건 조회")
    public RsData<List<BlogDraftDto>> getDrafts(@AuthenticationPrincipal SecurityUser userDetails) {
        List<BlogDraftDto> draftDtos = blogService.findDraftsByUserId(userDetails.getId());
        return RsData.of("200-2", "블로그 임시저장 글 조회가 완료되었습니다.", draftDtos);
    }

    @DeleteMapping("/drafts/{blogId}")
    @Operation(summary = "블로그 임시저장 글 삭제")
    public RsData<Void> deleteDrafts(
            @PathVariable Long blogId,
            @AuthenticationPrincipal SecurityUser userDetails
    ) {
        blogService.deleteDraft(userDetails.getId(), blogId);
        return new RsData<>("200-3", "블로그 임시저장 글 삭제가 완료되었습니다.");
    }
}
