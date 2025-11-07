package com.back.domain.blog.blog.controller;

import com.back.domain.blog.blog.dto.BlogDto;
import com.back.domain.blog.blog.dto.BlogWriteReqDto;
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

    @PostMapping("/write")
    @Operation(summary = "블로그 글 작성")
    public RsData<BlogDto> write(
            @Valid @RequestBody BlogWriteReqDto request
    ) {
        // User actor = rq.getActor();

        return new RsData<>("200-1", "블로그 글 작성이 완료되었습니다.");
    }

    @GetMapping("/{id}")
    public RsData<BlogDto> getItem(@PathVariable Long id) {
        // User actor = rq.getActor();
        BlogDto dto = blogService.getItem(id);
        return new RsData<>("200-2", "블로그 글 조회가 완료되었습니다.", dto);
    }


}
