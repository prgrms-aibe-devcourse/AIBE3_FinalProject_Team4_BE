package com.back.domain.shared.link.service;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.link.dto.BlogShorlogLinkResponse;
import com.back.domain.blog.link.dto.LinkedShorlogSummaryResponse;
import com.back.domain.blog.link.dto.MyBlogSummaryResponse;
import com.back.domain.shared.link.entity.ShorlogBlogLink;
import com.back.domain.shared.link.exception.LinkErrorCase;
import com.back.domain.shared.link.repository.ShorlogBlogLinkRepository;
import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.domain.shorlog.shorlogbloglink.dto.MyShorlogSummaryResponse;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShorlogBlogLinkService {

    private final ShorlogBlogLinkRepository shorlogBlogLinkRepository;
    private final ShorlogRepository shorlogRepository;
    private final BlogRepository blogRepository;

    // 블로그 연결
    @Transactional
    public void linkBlog(Long shorlogId, Long blogId, Long userId) {
        Shorlog shorlog = shorlogRepository.findById(shorlogId)
                .orElseThrow(() -> new ServiceException(LinkErrorCase.SHORLOG_NOT_FOUND));

        if (!shorlog.getUser().getId().equals(userId)) {
            throw new ServiceException(LinkErrorCase.FORBIDDEN);
        }

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ServiceException(LinkErrorCase.BLOG_NOT_FOUND));

        if (!blog.getUser().getId().equals(userId)) {
            throw new ServiceException(LinkErrorCase.FORBIDDEN);
        }

        // 동일한 숏로그-블로그 조합의 중복 연결 방지
        shorlogBlogLinkRepository.findByShorlogIdAndBlogId(shorlogId, blogId)
                .ifPresent(existingLink -> {
                    throw new ServiceException(LinkErrorCase.ALREADY_LINKED);
                });


        ShorlogBlogLink link = ShorlogBlogLink.create(shorlog, blog);
        shorlogBlogLinkRepository.save(link);
    }

    // 블로그 연결 해제
    @Transactional
    public void unlinkBlog(Long shorlogId, Long blogId, Long userId) {
        Shorlog shorlog = shorlogRepository.findById(shorlogId)
                .orElseThrow(() -> new ServiceException(LinkErrorCase.SHORLOG_NOT_FOUND));

        if (!shorlog.getUser().getId().equals(userId)) {
            throw new ServiceException(LinkErrorCase.FORBIDDEN);
        }

        ShorlogBlogLink link = shorlogBlogLinkRepository.findByShorlogIdAndBlogId(shorlogId, blogId)
                .orElseThrow(() -> new ServiceException(LinkErrorCase.LINK_NOT_FOUND));

        shorlogBlogLinkRepository.delete(link);
    }

    @Transactional
    public BlogShorlogLinkResponse linkShorlog(Long blogId, Long shorlogId, Long userId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ServiceException(LinkErrorCase.BLOG_NOT_FOUND));
        if (!blog.getUser().getId().equals(userId)) {
            throw new ServiceException(LinkErrorCase.FORBIDDEN);
        }
        Shorlog shorlog = shorlogRepository.findById(shorlogId)
                .orElseThrow(() -> new ServiceException(LinkErrorCase.SHORLOG_NOT_FOUND));
        if (!shorlog.getUser().getId().equals(userId)) {
            throw new ServiceException(LinkErrorCase.FORBIDDEN);
        }
        shorlogBlogLinkRepository.findByShorlogIdAndBlogId(shorlogId, blogId)
                .ifPresent(existingLink -> {
                    throw new ServiceException(LinkErrorCase.ALREADY_LINKED);
                });

        ShorlogBlogLink link = ShorlogBlogLink.create(shorlog, blog);
        shorlogBlogLinkRepository.save(link);
        int count = shorlogBlogLinkRepository.countByBlogId(blogId);
        return new BlogShorlogLinkResponse(blogId, shorlogId, true, count);
    }

    @Transactional
    public BlogShorlogLinkResponse unlinkShorlog(Long blogId, Long shorlogId, Long userId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ServiceException(LinkErrorCase.BLOG_NOT_FOUND));
        if (!blog.getUser().getId().equals(userId)) {
            throw new ServiceException(LinkErrorCase.FORBIDDEN);
        }
        ShorlogBlogLink link = shorlogBlogLinkRepository.findByShorlogIdAndBlogId(shorlogId, blogId)
                .orElseThrow(() -> new ServiceException(LinkErrorCase.LINK_NOT_FOUND));
        shorlogBlogLinkRepository.delete(link);
        int count = shorlogBlogLinkRepository.countByBlogId(blogId);
        boolean linked = count > 0;
        return new BlogShorlogLinkResponse(blogId, shorlogId, linked, count);
    }
    
    public List<MyBlogSummaryResponse> getRecentBlogByAuthor(Long userId, int size) {
        List<Blog> blogs = blogRepository.findRecentBlogsByUserId(userId, PageRequest.of(0, size));
        return blogs.stream()
                .map(MyBlogSummaryResponse::new)
                .toList();
    }

    public List<MyShorlogSummaryResponse> getRecentShorlogByAuthor(Long userId, int size) {
        List<Shorlog> shorlogs = shorlogRepository.findRecentShorlogsByUserId(userId);
        return shorlogs.stream()
                .limit(size)
                .map(MyShorlogSummaryResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LinkedShorlogSummaryResponse> getLinkedShorlogs(Long blogId, Long userId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ServiceException(LinkErrorCase.BLOG_NOT_FOUND));
        if (!blog.getUser().getId().equals(userId)) {
            throw new ServiceException(LinkErrorCase.FORBIDDEN);
        }
        List<ShorlogBlogLink> links = shorlogBlogLinkRepository.findByBlogId(blogId);
        List<Long> shorlogIds = links.stream()
                .map(ShorlogBlogLink::getShorlog)
                .map(Shorlog::getId)
                .toList();

        List<Shorlog> shorlogs = shorlogRepository.findAllById(shorlogIds);

        return shorlogs.stream()
                .map(LinkedShorlogSummaryResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Long> getLinkedBlogIds(Long shorlogId, Long userId) {
        Shorlog shorlog = shorlogRepository.findById(shorlogId)
                .orElseThrow(() -> new ServiceException(LinkErrorCase.SHORLOG_NOT_FOUND));
        if (!shorlog.getUser().getId().equals(userId)) {
            throw new ServiceException(LinkErrorCase.FORBIDDEN);
        }
        return shorlogBlogLinkRepository.findBlogIdsByShorlogId(shorlogId);
    }
}

