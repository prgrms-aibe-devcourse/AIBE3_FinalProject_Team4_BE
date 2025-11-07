package com.back.domain.blog.blog.service;

import com.back.domain.blog.blog.dto.BlogDto;
import com.back.domain.blog.blog.dto.BlogWriteReqDto;
import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogService {
    private final BlogRepository blogRepository;

    public void truncate() {
        blogRepository.deleteAll();
    }

    public List<Blog> finAll() {
        return blogRepository.findAll();
    }

    public void save(String title, String content) {
        this.blogRepository.save(Blog.builder()
                .title(title)
                .content(content)
                .build());
    }

    public BlogDto getItem(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 블로그 글입니다."));

        return toDto(blog);
    }

    @Transactional
    public BlogDto write(BlogWriteReqDto reqDto) {
        Blog blog = Blog.create(reqDto.blog(), reqDto.hashtagIds());

        Blog saved = blogRepository.save(blog);
        return toDto(saved);
    }

    @Transactional
    public BlogDto modify(Long id, BlogWriteReqDto reqDto) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ServiceException("404-3", "블로그를 찾을 수 없습니다."));

        blog.modify(reqDto.blog(), reqDto.hashtagIds());

        return toDto(blog);
    }

    @Transactional
    public void increaseView(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ServiceException("404-3", "블로그를 찾을 수 없습니다."));

        blog.increaseViewCount();
    }

    @Transactional
    public void increaseLike(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ServiceException("404-3", "블로그를 찾을 수 없습니다."));

        blog.increaseLikeCount();
    }

    @Transactional
    public void delete(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ServiceException("404-3", "블로그를 찾을 수 없습니다."));

        blogRepository.delete(blog);
    }

    private BlogDto toDto(Blog blog) {
        return new BlogDto(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getThumbnailUrl(),
                blog.getHashtags(),
                blog.getStatus().name(),
                blog.getViewCount(),
                blog.getLikeCount(),
                blog.getBookmarkCount(),
                false, // isLiked, isBookmarked는 User 연동 후 처리
                false,
                0,
                blog.getCreatedAt(),
                blog.getModifiedAt(),
                null
        );
    }


}

