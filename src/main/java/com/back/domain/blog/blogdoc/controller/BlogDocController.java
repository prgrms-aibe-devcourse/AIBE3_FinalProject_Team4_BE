package com.back.domain.blog.blogdoc.controller;

import com.back.domain.blog.blogdoc.document.BlogDoc;
import com.back.domain.blog.blogdoc.dto.BlogDocWriteRequest;
import com.back.domain.blog.blogdoc.service.BlogDocService;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/blog")
@RestController
@RequiredArgsConstructor
@Validated
public class BlogDocController {
    private final BlogDocService postDocService;

    @PostMapping("/write")
    public RsData<BlogDoc> write(
            @RequestBody @Valid BlogDocWriteRequest writeRequest

    ) {
        BlogDoc blogDoc = postDocService.write(writeRequest.title(), writeRequest.content());
        return new RsData("201-1", "블로그 글 작성이 완료되었습니다.", blogDoc);
    }

    @GetMapping("/search")
    public List<BlogDoc> search(@RequestParam("keyword") String keyword) {
        return postDocService.searchByKeyword(keyword);
    }


}
