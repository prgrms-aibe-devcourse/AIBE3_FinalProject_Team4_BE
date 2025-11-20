package com.back.domain.blog.blogdoc.repository;

import com.back.domain.blog.blogdoc.dto.BlogSearchCondition;
import com.back.domain.blog.blogdoc.dto.BlogSearchResult;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogDocRepository {
    BlogSearchResult searchBlogs(BlogSearchCondition condition);
}
