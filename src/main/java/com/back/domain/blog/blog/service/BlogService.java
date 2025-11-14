package com.back.domain.blog.blog.service;

import com.back.domain.blog.blog.dto.BlogDraftDto;
import com.back.domain.blog.blog.dto.BlogDto;
import com.back.domain.blog.blog.dto.BlogWriteReqDto;
import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogStatus;
import com.back.domain.blog.blog.exception.BlogErrorCase;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.shared.hashtag.entity.Hashtag;
import com.back.domain.shared.hashtag.repository.HashtagRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogService {
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final HashtagRepository hashtagRepository;

    public void truncate() {
        blogRepository.deleteAll();
    }

    public List<BlogDto> findAll() {
        List<Blog> blogs = blogRepository.findAll();
        return blogs.stream()
                .map(b -> new BlogDto(b))
                .toList();
    }

    @Transactional
    public BlogDto findById(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));

        return new BlogDto(blog);
    }

    @Transactional
    public Blog write(Long userId, BlogWriteReqDto reqBody, String thumbnailUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.PERMISSION_DENIED));
        Blog blog = new Blog(user, reqBody.title(), reqBody.content(), reqBody.thumbnailUrl(), BlogStatus.PUBLISHED);

        List<Hashtag> hashtags = reqBody.hashtagNames().stream()
                .map(name -> hashtagRepository.findByName(name)
                        .orElseGet(() -> hashtagRepository.save(new Hashtag(name))))
                .toList();

        blog.updateHashtags(hashtags);

        return blogRepository.save(blog);
    }

    @Transactional
    public BlogDto modify(Long userId, Long blogId, BlogWriteReqDto reqBody, String thumbnailUrl) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new ServiceException(BlogErrorCase.PERMISSION_DENIED);
        }
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));
        if (!blog.getUser().getId().equals(userId)) {
            throw new ServiceException(BlogErrorCase.PERMISSION_DENIED);
        }
        List<Hashtag> hashtags = reqBody.hashtagNames().stream()
                .map(name -> hashtagRepository.findByName(name)
                        .orElseGet(() -> hashtagRepository.save(new Hashtag(name))))
                .toList();

        blog.modify(reqBody, reqBody.hashtagNames());
        blog.updateHashtags(hashtags);

        return new BlogDto(blog);
    }

    @Transactional
    public long increaseView(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));

        blog.increaseViewCount();
        return blog.getViewCount();
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));
        if (!blog.getUser().getId().equals(userId)) {
            throw new ServiceException(BlogErrorCase.PERMISSION_DENIED);
        }
        blogRepository.delete(blog);
    }

    @Transactional
    public Blog saveDraft(Long userId, @Valid BlogWriteReqDto reqbody, String thumbnailUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.PERMISSION_DENIED));

        Blog blog = createDraft(userId, reqbody, thumbnailUrl);     // 존재하지 않으면 새로 생성
        updateDraft(blog, reqbody, thumbnailUrl);

        return blog;
    }

    private Blog createDraft(Long userId, BlogWriteReqDto reqBody, String thumbnailUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.PERMISSION_DENIED));

        Blog newBlog = Blog.create(user, reqBody.title(), reqBody.content(), thumbnailUrl, BlogStatus.DRAFT);

        List<Hashtag> hashtags = reqBody.hashtagNames().stream()
                .map(name -> hashtagRepository.findByName(name)
                        .orElseGet(() -> hashtagRepository.save(new Hashtag(name))))
                .toList();
        newBlog.updateHashtags(hashtags);
        newBlog.setStatus(BlogStatus.DRAFT);
        return blogRepository.save(newBlog);
    }

    private void updateDraft(Blog blog, BlogWriteReqDto req, String thumbnailUrl) {
        blog.modify(req, req.hashtagNames());
        blog.setStatus(BlogStatus.DRAFT);
    }

    public List<BlogDraftDto> findDraftsByUserId(Long userId) {
        List<Blog> drafts = blogRepository.findByStatusAndUserId(BlogStatus.DRAFT, userId);
        return drafts.stream()
                .map(b -> new BlogDraftDto(b))
                .toList();
    }

    public List<BlogDto> findAllByUserId(Long userId) {
        List<Blog> blogs = blogRepository.findAllByUserIdAndStatus(userId, BlogStatus.PUBLISHED);
        return blogs.stream()
                .map(b -> new BlogDto(b))
                .toList();
    }

    public void deleteDraft(Long id, Long draftId) {
        Blog blog = blogRepository.findById(draftId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));
        if (!blog.getUser().getId().equals(id)) {
            throw new ServiceException(BlogErrorCase.PERMISSION_DENIED);
        }
        blogRepository.delete(blog);
    }
}