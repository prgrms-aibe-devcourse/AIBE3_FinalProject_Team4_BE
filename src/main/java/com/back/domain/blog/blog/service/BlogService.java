package com.back.domain.blog.blog.service;

import com.back.domain.blog.blog.dto.BlogDraftDto;
import com.back.domain.blog.blog.dto.BlogDto;
import com.back.domain.blog.blog.dto.BlogWriteReqDto;
import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogStatus;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.global.exception.ServiceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogService {
    private final BlogRepository blogRepository;

    public static Blog create(@Valid BlogWriteReqDto blogDto, String thumbnailUrl) {
        if (blogDto.title() == null || blogDto.title().isBlank()) {
            throw new ServiceException("400-1", "블로그 글의 제목은 필수입니다.");
        }
        Blog blog = Blog.builder()
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
                .orElseThrow(() -> new ServiceException("404-2", "존재하지 않는 블로그 글입니다."));

        return new BlogDto(blog);
    }

    @Transactional
    public Blog write(BlogWriteReqDto reqBody, String thumbnailurl) {
        Blog blog = create(reqBody, thumbnailurl);
        blog.updateHashtags(reqBody.hashtagIds());
        blogRepository.save(blog);
        return blog;
    }

    @Transactional
    public BlogDto modify(Long id, BlogWriteReqDto reqDto, String thumbnailUrl) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ServiceException("404-2", "존재하지 않는 블로그 글입니다."));
        blog.modify(reqDto, reqDto.hashtagIds());
        blogRepository.save(blog);
        return new BlogDto(blog);
    }

    @Transactional
    public void increaseView(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ServiceException("404-2", "존재하지 않는 블로그 글입니다."));

        blog.increaseViewCount();
    }

    @Transactional
    public void increaseLike(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ServiceException("404-2", "존재하지 않는 블로그 글입니다."));

        blog.increaseLikeCount();
    }

    @Transactional
    public void delete(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ServiceException("404-2", "존재하지 않는 블로그 글입니다."));

        blogRepository.delete(blog);
    }

    @Transactional
    public Blog saveDraft(Long id, @Valid BlogWriteReqDto reqbody, String thumbnailUrl) {
        Blog blog;
        if (blogRepository.findById(id).isEmpty()) {
            blog = create(reqbody, thumbnailUrl);
            blog.updateHashtags(reqbody.hashtagIds());
            blog.setStatus(BlogStatus.DRAFT);
            blogRepository.save(blog);
            return blog;
        } else {
            blog = blogRepository.findById(id)
                    .orElseThrow(() -> new ServiceException("404-2", "존재하지 않는 블로그 글입니다."));
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

