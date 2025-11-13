package com.back.domain.blog.like.service;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.like.entity.BlogLike;
import com.back.domain.blog.like.repository.BlogLikeRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogLikeService {
    private final BlogLikeRepository likeRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean likeOn(Long userId, Long blogId) {
        if (likeRepository.existsByBlog_IdAndUser_Id(blogId, userId)) {
            return true;
        }
        BlogLike like = new BlogLike();
        like.setBlog(new Blog());
        like.getBlog().setId(blogId);
        like.setUser(new User());
        like.getUser().setId(userId);

        try {
            likeRepository.save(like);
            blogRepository.increaseBookmark(blogId);
            return true;
        } catch (DataIntegrityViolationException e) {
            return true;
        }
    }

    @Transactional
    public boolean likeOff(Long userId, Long blogId) {
        long deleted = likeRepository.deleteByBlog_IdAndUser_Id(blogId, userId);
        if (deleted > 0) {
            blogRepository.decreaseBookmark(blogId);
            return true;
        }
        return false;
    }

    public boolean isLike(Long blogId, Long userId) {
        return likeRepository.existsByBlogIdAndUserId(blogId, userId);
    }

    public long getLikeCount(Long blogId) {
        return likeRepository.countBlogBookmarkBy(blogId);
    }
}
