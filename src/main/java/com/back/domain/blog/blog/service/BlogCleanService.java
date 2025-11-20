package com.back.domain.blog.blog.service;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogStatus;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.blogdoc.service.BlogDocIndexer;
import com.back.domain.shared.image.service.ImageLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogCleanService {

    private final BlogRepository blogRepository;
    private final ImageLifecycleService imageLifecycleService;
    private final BlogDocIndexer blogDocIndexer;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteExpiredDrafts() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        List<Blog> expiredDrafts =
                blogRepository.findByStatusAndModifiedAtBefore(BlogStatus.DRAFT, cutoff);

        for (Blog blog : expiredDrafts) {
            imageLifecycleService.decrementReference(blog.getThumbnailUrl());
            blogDocIndexer.delete(blog.getId());
            blogRepository.delete(blog);
        }
    }
}