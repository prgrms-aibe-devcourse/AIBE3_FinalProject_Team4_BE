package com.back.domain.blog.bookmark.service;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.bookmark.entity.BlogBookmark;
import com.back.domain.blog.bookmark.repository.BlogBookmarkRepository;
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogBookmarkService {

    private final BlogBookmarkRepository bookmarkRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public boolean bookmarkOn(Long userId, Long blogId) {
        if (bookmarkRepository.existsByBlog_IdAndUser_Id(blogId, userId)) {
            return true; // 이미 ON → 멱등
        }

        // blog 조회 (receiver 확인 필요)
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("블로그 게시글을 찾을 수 없습니다."));

        // 자기 글 북마크 금지
        if (blog.getUser().getId().equals(userId)) {
            throw new RuntimeException("본인의 글은 북마크할 수 없습니다.");
        }

        BlogBookmark r = new BlogBookmark();
        r.setBlog(new Blog());
        r.getBlog().setId(blogId);
        r.setUser(new User());
        r.getUser().setId(userId);

        try {
            bookmarkRepository.save(r);
            blogRepository.increaseBookmark(blogId);

            // 🔔 북마크 알림
            User sender = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            notificationService.send(
                    blog.getUser().getId(),          // receiver
                    userId,                          // sender
                    NotificationType.BLOG_BOOKMARK,  // type
                    blogId,                          // targetId
                    sender.getNickname()
            );

            return true;
        } catch (DataIntegrityViolationException e) {
            return true;
        }
    }

    @Transactional
    public boolean bookmarkOff(Long userId, Long blogId) {
        long deleted = bookmarkRepository.deleteByBlog_IdAndUser_Id(blogId, userId);
        if (deleted > 0) {
            blogRepository.decreaseBookmark(blogId);
            return true;
        }
        return false;
    }

    public boolean isBookmarked(Long blogId, Long userId) {
        return bookmarkRepository.existsByBlogIdAndUserId(blogId, userId);
    }

    public long getBookmarkCount(Long blogId) {
        return bookmarkRepository.countBlogBookmarkBy(blogId);
    }
}