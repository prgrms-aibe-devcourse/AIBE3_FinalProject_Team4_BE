package com.back.domain.blog.like.service;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.exception.BlogErrorCase;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.blogdoc.service.BlogDocIndexer;
import com.back.domain.blog.like.entity.BlogLike;
import com.back.domain.blog.like.repository.BlogLikeRepository;
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogLikeService {
    private final BlogLikeRepository likeRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final BlogDocIndexer blogDocIndexer;

    @Transactional
    public boolean likeOn(Long userId, Long blogId) {
        if (likeRepository.existsByBlog_IdAndUser_Id(blogId, userId)) {
            return true;
        }
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new IllegalArgumentException("블로그 게시글을 찾을 수 없습니다."));
        // 자기 글 좋아요 금지
        if (blog.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 글에는 좋아요할 수 없습니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.PERMISSION_DENIED));
        BlogLike like = new BlogLike(blog, user);

        try {
            likeRepository.save(like);
            blogRepository.incrementLikeCount(blogId);
            blogDocIndexer.index(blogId);
            // 🔔 알림 전송
            User sender = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            notificationService.send(
                    blog.getUser().getId(),
                    userId,                         // sender
                    NotificationType.BLOG_LIKE,
                    blogId,                         // target
                    sender.getNickname()
            );
            return true;
        } catch (DataIntegrityViolationException e) {
            return true;
        }
    }

    @Transactional
    public long likeOff(Long userId, Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.PERMISSION_DENIED));
        BlogLike blogLike = likeRepository.findByBlogIdAndUserId(blog.getId(), user.getId())
                .orElseThrow(() -> new ServiceException(BlogErrorCase.REACTION_NOT_FOUND));

        blogRepository.decrementLikeCount(blogId);
        likeRepository.delete(blogLike);
        blogDocIndexer.index(blogId);
        return blogRepository.getLikeCountById(blogId);
    }

    public long getLikeCount(Long blogId) {
        return blogRepository.getLikeCountById(blogId);
    }

    public boolean isLiked(Long id, Long userId) {
        return likeRepository.existsByBlog_IdAndUser_Id(id, userId);
    }

    public Set<Long> findLikedBlogIds(Long userId, List<Long> blogIds) {
        if (userId == null) return Set.of();
        return likeRepository.findLikedBlogIdsByUserId(blogIds, userId);
    }
}
