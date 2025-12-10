package com.back.domain.blog.bookmark.service;

import com.back.domain.blog.blog.dto.BlogIndexEvent;
import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.exception.BlogErrorCase;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.bookmark.entity.BlogBookmark;
import com.back.domain.blog.bookmark.repository.BlogBookmarkRepository;
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogBookmarkService {

    private final BlogBookmarkRepository bookmarkRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public boolean bookmarkOn(Long userId, Long blogId) {
        if (bookmarkRepository.existsByBlog_IdAndUser_Id(blogId, userId)) {
            return true;
        }
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new IllegalArgumentException("블로그 게시글을 찾을 수 없습니다."));

        if (blog.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 글은 북마크할 수 없습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.PERMISSION_DENIED));

        BlogBookmark bookmark = new BlogBookmark(blog, user);
        try {
            bookmarkRepository.save(bookmark);
            blogRepository.incrementBookmarkCount(blogId);
            eventPublisher.publishEvent(new BlogIndexEvent(blogId));
            // 🔔 북마크 알림
            User sender = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            notificationService.send(
                    blog.getUser().getId(),
                    userId,
                    NotificationType.BLOG_BOOKMARK,
                    blogId,
                    sender.getNickname()
            );
        } catch (
                DataIntegrityViolationException e) {
            return true;
        }
        return true;
    }

    @Transactional
    public long bookmarkOff(Long userId, Long blogId) {
        if (blogRepository.findById(blogId).isEmpty()) {
            throw new ServiceException(BlogErrorCase.BLOG_NOT_FOUND);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.PERMISSION_DENIED));
        BlogBookmark bookmark = bookmarkRepository.findByBlogIdAndUserId(blogId, user.getId())
                .orElseThrow(() -> new ServiceException(BlogErrorCase.REACTION_NOT_FOUND));

        blogRepository.decrementBookmarkCount(blogId);
        bookmarkRepository.delete(bookmark);

        eventPublisher.publishEvent(new BlogIndexEvent(blogId));
        return blogRepository.getBookmarkCountById(blogId);
    }

    public boolean isBookmarked(Long blogId, Long userId) {
        if (userId == null) return false;
        return bookmarkRepository.existsByBlogIdAndUserId(blogId, userId);
    }

    @Transactional
    public long getBookmarkCount(Long blogId) {
        return blogRepository.getBookmarkCountById(blogId);
    }

    public Set<Long> findBookmarkedBlogIds(Long userId, List<Long> blogIds) {
        if (userId == null) return Set.of();
        if (blogIds == null || blogIds.isEmpty()) return Set.of();
        return bookmarkRepository.findBookmarkedBlogIdsByUserId(blogIds, userId);
    }
}