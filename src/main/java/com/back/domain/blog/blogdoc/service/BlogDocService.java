package com.back.domain.blog.blogdoc.service;

import com.back.domain.blog.blogdoc.dto.BlogSearchCondition;
import com.back.domain.blog.blogdoc.dto.BlogSearchResult;
import com.back.domain.blog.blogdoc.dto.BlogSliceResponse;
import com.back.domain.blog.blogdoc.dto.BlogSummaryResponse;
import com.back.domain.blog.blogdoc.repository.BlogDocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogDocService {
    private final BlogDocRepository blogDocRepository;

    public BlogSliceResponse<BlogSummaryResponse> searchBlogs(BlogSearchCondition condition) {

        BlogSearchResult result = blogDocRepository.searchBlogs(condition);

        List<BlogSummaryResponse> content = result.docs().stream()
                .map(BlogSummaryResponse::new)
                .toList();

        return new BlogSliceResponse<>(
                content,
                result.hasNext(),
                result.nextCursor()
        );
    }
}