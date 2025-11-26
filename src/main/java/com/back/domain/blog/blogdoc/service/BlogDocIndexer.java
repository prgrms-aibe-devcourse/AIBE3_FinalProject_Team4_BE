package com.back.domain.blog.blogdoc.service;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogStatus;
import com.back.domain.blog.blog.exception.BlogErrorCase;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.blogdoc.document.BlogDoc;
import com.back.domain.blog.blogdoc.repository.BlogDocRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogDocIndexer {
    private final BlogDocRepository blogDocRepository;
    private final BlogRepository blogRepository;

    public void index(Long blogId) {
        Blog blog = blogRepository.findForIndexingById(blogId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));
        if (blog.getStatus() != BlogStatus.PUBLISHED) {
            blogDocRepository.deleteById(blogId);
            return;
        }
        BlogDoc doc = BlogDoc.from(blog);
        blogDocRepository.save(doc);
    }

    public void delete(Long blogId) {
        blogDocRepository.deleteById(blogId);
    }
}