package com.back.domain.blog.blog.repository;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogMySortType;
import com.back.domain.blog.blog.entity.BlogStatus;

import java.util.List;

public interface BlogRepositoryCustom {

    List<Blog> findAllByUserIdAndStatusWithSort(
            Long userId,
            BlogStatus status,
            BlogMySortType sortType
    );
}