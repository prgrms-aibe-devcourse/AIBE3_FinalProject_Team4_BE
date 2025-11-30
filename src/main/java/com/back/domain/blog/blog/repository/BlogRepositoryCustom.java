package com.back.domain.blog.blog.repository;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogMySortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlogRepositoryCustom {
    Page<Blog> findMyBlogs(Long userId, BlogMySortType sortType, Pageable pageable);

    Page<Blog> findByUserId(Long userId, BlogMySortType sortType, Pageable pageable);
}