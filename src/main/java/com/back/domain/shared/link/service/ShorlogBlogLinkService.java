package com.back.domain.shared.link.service;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.shared.link.entity.ShorlogBlogLink;
import com.back.domain.shared.link.exception.LinkErrorCase;
import com.back.domain.shared.link.repository.ShorlogBlogLinkRepository;
import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

        // 기존 연결이 있으면 삭제
        shorlogBlogLinkRepository.findByShorlogIdAndBlogId(shorlogId, blogId)
                .ifPresent(existingLink -> {
                    throw new ServiceException(LinkErrorCase.ALREADY_LINKED);
                });

        // 기존 다른 블로그와의 연결이 있으면 자동 해제
        Optional<Long> existingBlogId = shorlogBlogLinkRepository.findBlogIdByShorlogId(shorlogId);
        existingBlogId.ifPresent(existingId -> {
            ShorlogBlogLink existingLink = shorlogBlogLinkRepository.findByShorlogIdAndBlogId(shorlogId, existingId)
                    .orElseThrow(() -> new ServiceException(LinkErrorCase.LINK_NOT_FOUND));
            shorlogBlogLinkRepository.delete(existingLink);
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
    public void linkShorlog(Long shorlogId, Long blogId, Long userId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ServiceException(LinkErrorCase.BLOG_NOT_FOUND));
        if (!blog.getUser().getId().equals(userId)) {
            throw new ServiceException(LinkErrorCase.FORBIDDEN);
        }
        Shorlog shorlog = shorlogRepository.findById(blogId)
                .orElseThrow(() -> new ServiceException(LinkErrorCase.SHORLOG_NOT_FOUND));
        if (!shorlog.getUser().getId().equals(userId)) {
            throw new ServiceException(LinkErrorCase.FORBIDDEN);
        }

        shorlogBlogLinkRepository.findByShorlogIdAndBlogId(shorlogId, blogId)
                .ifPresent(existingLink -> {
                    throw new ServiceException(LinkErrorCase.ALREADY_LINKED);
                });

        Optional<Long> existingShorlogId = shorlogBlogLinkRepository.findShorlogIdsByBlogId(blogId);
        existingShorlogId.ifPresent(existingId -> {
            ShorlogBlogLink existingLink = shorlogBlogLinkRepository.findByShorlogIdAndBlogId(existingId, blogId)
                    .orElseThrow(() -> new ServiceException(LinkErrorCase.LINK_NOT_FOUND));
            shorlogBlogLinkRepository.delete(existingLink);
        });

        ShorlogBlogLink link = ShorlogBlogLink.create(shorlog, blog);
        shorlogBlogLinkRepository.save(link);
    }

    public void unlinkShorlog(Long blogId, Long shorlogId, Long userId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ServiceException(LinkErrorCase.BLOG_NOT_FOUND));
        if (!blog.getUser().getId().equals(userId)) {
            throw new ServiceException(LinkErrorCase.FORBIDDEN);
        }
        ShorlogBlogLink link = shorlogBlogLinkRepository.findByShorlogIdAndBlogId(shorlogId, blogId)
                .orElseThrow(() -> new ServiceException(LinkErrorCase.LINK_NOT_FOUND));
        shorlogBlogLinkRepository.delete(link);
    }
}

