package com.back.domain.blog.blog.service;

import com.back.domain.blog.blog.dto.BlogDraftDto;
import com.back.domain.blog.blog.dto.BlogDto;
import com.back.domain.blog.blog.dto.BlogWriteReqDto;
import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogStatus;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogService {
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    public static Blog create(Long id, @Valid BlogWriteReqDto blogDto, String thumbnailUrl) {
        if (blogDto.title() == null || blogDto.title().isBlank()) {
            throw new ServiceException("400-1", "블로그 글의 제목은 필수입니다.");
        }
        Blog blog = Blog.builder()
                .userId(id)
                .title(blogDto.title())
                .content(blogDto.content())
                .thumbnailUrl(thumbnailUrl)
                .status(blogDto.status())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .viewCount(0)
                .likeCount(0)
                .bookmarkCount(0)
                .commentCount(0)
                .build();

        return blog;
    }

    public void truncate() {
        blogRepository.deleteAll();
    }

    public List<Blog> finAll() {
        return blogRepository.findAll();
    }

    @Transactional
    public BlogDto findById(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);

        return new BlogDto(blog);
    }

    @Transactional
    public Blog write(Long userId, BlogWriteReqDto reqBody, String thumbnailurl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("404-3", "존재하지 않는 사용자입니다."));
        Blog blog = create(user.getId(), reqBody, thumbnailurl);
        blog.updateHashtags(reqBody.hashtagIds());
        blogRepository.save(blog);
        return blog;
    }

    @Transactional
    public BlogDto modify(Long userId, Long blogId, BlogWriteReqDto reqDto, String thumbnailUrl) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new ServiceException("404-3", "존재하지 않는 사용자입니다.");
        }
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(NoSuchElementException::new);
        blog.modify(reqDto, reqDto.hashtagIds());

        return new BlogDto(blog);
    }

    @Transactional
    public long increaseView(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);

        blog.increaseViewCount();
        return blog.getViewCount();
    }

    @Transactional
    public long increaseLike(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);

        blog.increaseLikeCount();
        return blog.getLikeCount();
    }

    @Transactional
    public void delete(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);

        blogRepository.delete(blog);
    }

    @Transactional
    public Blog saveDraft(Long userId, Long id, @Valid BlogWriteReqDto reqbody, String thumbnailUrl) {
        Blog blog;
        if (blogRepository.findById(id).isEmpty()) {
            blog = create(userId, reqbody, thumbnailUrl);
            blog.updateHashtags(reqbody.hashtagIds());
            blog.setStatus(BlogStatus.DRAFT);
            blogRepository.save(blog);
            return blog;
        } else {
            blog = blogRepository.findById(id)
                    .orElseThrow(NoSuchElementException::new);
            blog.modify(reqbody, reqbody.hashtagIds());
            blogRepository.save(blog);
            return blog;
        }
    }

    public List<BlogDraftDto> findDraftsByUserId(Long userId) {
        List<Blog> drafts = blogRepository.findByStatusAndUserId(BlogStatus.DRAFT, userId);
        return drafts.stream()
                .map(b -> new BlogDraftDto(b))
                .toList();
    }
}

