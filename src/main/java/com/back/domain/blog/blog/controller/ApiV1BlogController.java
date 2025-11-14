package com.back.domain.blog.blog.controller;

import com.back.domain.blog.blog.dto.BlogDraftDto;
import com.back.domain.blog.blog.dto.BlogDto;
import com.back.domain.blog.blog.dto.BlogWriteReqDto;
import com.back.domain.blog.blog.entity.Blog;
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
    public RsData<BlogDto> create(
            @Valid @RequestBody BlogWriteReqDto reqbody,
            @RequestParam(required = false) String thumbnailUrl,
            @AuthenticationPrincipal SecurityUser userDetails
    ) {
        Blog blog = blogService.write(userDetails.getId(), reqbody, thumbnailUrl);

        return new RsData<>("201-1", "블로그 글 작성이 완료되었습니다.", new BlogDto(blog));
    }

    //TODO: 아래 두개의 api 추후 pagination, 검색/filtering api만들 예정
    @GetMapping("")
    @Operation(summary = "블로그 글 다건 조회")
    public RsData<List<BlogDto>> getItems() {
        List<BlogDto> blogDtos = blogService.findAll();
        return new RsData<>("200-1", "블로그 글 조회가 완료되었습니다.", blogDtos);
    }

    @GetMapping("/my")
    @Operation(summary = "내 블로그 글 다건 조회")
    public RsData<List<BlogDto>> getMyItems(@AuthenticationPrincipal SecurityUser userDetails) {
        List<BlogDto> blogDtos = blogService.findAllByUserId(userDetails.getId());
        return new RsData<>("200-1", "내 블로그 글 조회가 완료되었습니다.", blogDtos);
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
    public RsData<Void> delete(@PathVariable Long id, @AuthenticationPrincipal SecurityUser userDetails) {
        blogService.delete(id, userDetails.getId());
        return new RsData<>("200-4", "블로그 글 삭제가 완료되었습니다.");
    }

    @PostMapping("/drafts")
    @Operation(summary = "블로그 임시저장")
    public RsData<BlogDto> saveDraft(
            @Valid @RequestBody BlogWriteReqDto reqbody,
            @RequestParam(required = false) String thumbnailUrl,
            @AuthenticationPrincipal SecurityUser userDetails
    ) {
        Blog blog = blogService.saveDraft(userDetails.getId(), reqbody, thumbnailUrl);
        return new RsData<>("201-1", "블로그 임시저장이 완료되었습니다.", new BlogDto(blog));
    }

    @GetMapping("/drafts")
    @Operation(summary = "블로그 임시저장 글 다건 조회")
    public RsData<List<BlogDraftDto>> getDrafts(@AuthenticationPrincipal SecurityUser userDetails) {
        List<BlogDraftDto> draftDtos = blogService.findDraftsByUserId(userDetails.getId());

        return new RsData<>("200-2", "블로그 임시저장 글 조회가 완료되었습니다.", draftDtos);
    }

    @DeleteMapping("/drafts/{draftId}")
    @Operation(summary = "블로그 임시저장 글 삭제")
    public RsData<Void> deleteDrafts(
            @PathVariable Long draftId,
            @AuthenticationPrincipal SecurityUser userDetails
    ) {
        blogService.deleteDraft(userDetails.getId(), draftId);
        return new RsData<>("200-3", "블로그 임시저장 글 삭제가 완료되었습니다.");
    }
}
