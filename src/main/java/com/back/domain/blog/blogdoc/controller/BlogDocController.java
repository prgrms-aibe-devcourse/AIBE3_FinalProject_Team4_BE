package com.back.domain.blog.blogdoc.controller;

import com.back.domain.blog.blog.service.BlogService;
import com.back.domain.blog.blogdoc.document.BlogDoc;
import com.back.domain.blog.blogdoc.dto.BlogDocWriteRequest;
import com.back.domain.blog.blogdoc.service.BlogDocService;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/blog")
@RestController
@RequiredArgsConstructor
@Validated
public class BlogDocController {
    private final BlogDocService blogDocService;
    private final BlogService blogService;

    @PostMapping("/write")
    public RsData<BlogDoc> write(
            @RequestBody @Valid BlogDocWriteRequest writeRequest

    ) {
        BlogDoc blogDoc = blogDocService.write(writeRequest.title(), writeRequest.content());
        return new RsData("201-1", "블로그 글 작성이 완료되었습니다.", blogDoc);
    }

    //    TODO: 블로그 검색 API 추후 구현
//    @GetMapping("/search")
//    public List<BlogDoc> search(@RequestParam(required = false) String keyword,
//                                @RequestParam(required = false) Long categoryId,
//                                @RequestParam(required = false) List<String> hashtagName,
//                                @RequestParam(defaultValue = "false") Boolean followingOnly,
//                                @RequestParam(defaultValue = "latest") String sort,
//                                @RequestParam(defaultValue = "20") Integer size,
//                                @RequestParam(required = false) String cursor
//    ) {
//        List<Object> searchAfter = null;
//
//        BlogSearchReqDto request = new BlogSearchReqDto(
//                keyword,
//                categoryId,
//                hashtagName,
//                followingOnly,
//                sort,
//                searchAfter,
//                size,
//                null
//        );
//
//        BlogDto response = blogDocService.searchBlogs(
//                request,
//                user != null ? user.getId() : null
//        );
//        return blogDocService.searchByKeyword(keyword, size, cursor);
//    }


}
