package com.back.domain.blog.blog.controller;

import com.back.domain.blog.blog.dto.BlogDto;
import com.back.domain.blog.blog.dto.BlogWriteReqDto;
import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.service.BlogService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/blogs")
@RequiredArgsConstructor
public class ApiV1BlogController {
    private final BlogService blogService;
//    private final Rq rq;

    @PostMapping("")
    @Operation(summary = "블로그 글 작성")
    public RsData<BlogDto> create(
            @Valid @RequestBody BlogWriteReqDto reqbody,
            @RequestParam(required = false) String thumbnailUrl
    ) {
        Blog blog = blogService.write(reqbody, thumbnailUrl);
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
            @RequestParam(required = false) String thumbnailUrl) {
        BlogDto blogdto = blogService.modify(id, reqbody, thumbnailUrl);
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
            @RequestParam(required = false) String thumbnailUrl
    ) {
        Blog blog = blogService.saveDraft(blogId, reqbody, thumbnailUrl);
        return new RsData<>("201-2", "블로그 임시저장이 완료되었습니다.", new BlogDto(blog));
    }

//    @GetMapping("/drafts")
//    @Operation(summary = "블로그 임시저장 글 다건 조회")
//    public RsData<BlogDraftDto> getDrafts() {
//        User actor = rq.getActor();
//        List<BlogDraftDto> draftDtos = blogService.findDraftsByUserId(actor.getId());
//
//        return new RsData<>("200-5", "블로그 임시저장 글 조회가 완료되었습니다.", draftDtos);
//    }

}
