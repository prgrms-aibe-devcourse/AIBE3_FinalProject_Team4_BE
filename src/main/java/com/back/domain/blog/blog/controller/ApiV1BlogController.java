package com.back.domain.blog.blog.controller;

import com.back.domain.blog.blog.dto.BlogResponse;
import com.back.domain.blog.blog.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/blogs")
@RequiredArgsConstructor
public class ApiV1BlogController {
    private final BlogService blogService;

    @GetMapping
    public ResponseEntity<List<BlogResponse>> readAll() {
        return ResponseEntity.ok(blogService.finAll().stream()
                .map(post -> new BlogResponse(post.getId(), post.getTitle(), post.getContent()))
                .toList());
    }

    @PostMapping("/write")
    public String write() {
        blogService.write("테스트 제목", "테스트 내용");
        return "작성완료";
    }

}
