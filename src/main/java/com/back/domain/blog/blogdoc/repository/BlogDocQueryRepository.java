package com.back.domain.blog.blogdoc.repository;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.back.domain.blog.blogdoc.dto.BlogSearchCondition;
import com.back.domain.blog.blogdoc.dto.BlogSearchResult;

import java.util.List;

public interface BlogDocQueryRepository {
    BlogSearchResult searchBlogs(BlogSearchCondition condition, List<Long> authorIds, List<Query> recommendQueries);
}