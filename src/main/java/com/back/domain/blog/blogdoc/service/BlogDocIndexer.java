package com.back.domain.blog.blogdoc.service;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blogdoc.document.BlogDoc;
import com.back.domain.blog.blogdoc.repository.BlogDocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlogDocIndexer {
    private final BlogDocRepository blogDocRepository;

    public void index(Blog blog) {
        BlogDoc doc = BlogDoc.from(blog);
        blogDocRepository.save(doc);
    }

    public void delete(Long blogId) {
        blogDocRepository.deleteById(blogId);
    }
}