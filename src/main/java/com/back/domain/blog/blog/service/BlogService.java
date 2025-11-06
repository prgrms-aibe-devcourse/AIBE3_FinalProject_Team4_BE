package com.back.domain.blog.blog.service;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogService {
    private final BlogRepository blogRepository;
    public void write(String title, String content) {
        Blog post = Blog.builder()
                .title(title)
                .content(content)
                .build();

        blogRepository.save(post);
    }

    public void truncate() {
        blogRepository.deleteAll();
    }

    public List<Blog> finAll() {
        return blogRepository.findAll();
    }

    public void save(String title, String content) {
        this.blogRepository.save(Blog.builder()
                .title(title)
                .content(content)
                .build());
    }
}
