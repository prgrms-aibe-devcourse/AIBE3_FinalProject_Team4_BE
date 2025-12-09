package com.back.domain.blog.blog.service;

import com.back.domain.blog.blog.dto.BlogIndexDeleteEvent;
import com.back.domain.blog.blog.dto.BlogIndexEvent;
import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogStatus;
import com.back.domain.blog.blog.exception.BlogErrorCase;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.blogdoc.document.BlogDoc;
import com.back.domain.blog.blogdoc.repository.BlogDocRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BlogIndexEventHandler {

    private final BlogRepository blogRepository;
    private final BlogDocRepository blogDocRepository;

    // 트랜잭션 커밋 성공 후에 호출
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)// 새 트랜잭션에서 동작 엔티티 매니저도 새로 열림
    public void onBlogIndexEvent(BlogIndexEvent event) {
        Long blogId = event.blogId();

        Blog blog = blogRepository.findForIndexingById(blogId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));

        if (blog.getStatus() != BlogStatus.PUBLISHED) {
            blogDocRepository.deleteById(blogId);
            return;
        }

        BlogDoc doc = BlogDoc.from(blog);
        blogDocRepository.save(doc);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleDeleteEvent(BlogIndexDeleteEvent event) {
        Long blogId = event.blogId();
        blogDocRepository.deleteById(blogId);
    }
}