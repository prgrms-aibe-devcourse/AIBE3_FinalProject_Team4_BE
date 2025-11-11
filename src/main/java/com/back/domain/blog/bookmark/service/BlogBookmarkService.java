package com.back.domain.blog.bookmark.service;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.bookmark.entity.BlogBookmark;
import com.back.domain.blog.bookmark.repository.BlogBookmarkRepository;
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

    @Transactional
    public boolean bookmarkOn(Long userId, Long blogId) {
        if (bookmarkRepository.existsByBlog_IdAndUser_Id(blogId, userId)) {
            return true; // 이미 ON → 멱등
        }
        BlogBookmark r = new BlogBookmark();
        r.setBlog(new Blog());
        r.getBlog().setId(blogId);
        r.setUser(new User());
        r.getUser().setId(userId);

        try {
            bookmarkRepository.save(r);
            blogRepository.increaseBookmark(blogId);
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
            return true; // off 성공
        }
        return false; // 이미 off였음(멱등)
    }

    public boolean isBookmarked(Long blogId, Long userId) {
        return bookmarkRepository.existsByBlogIdAndUserId(blogId, userId);
    }

    public long getBookmarkCount(Long blogId) {
        return bookmarkRepository.countBlogBookmarkBy(blogId);
    }
}