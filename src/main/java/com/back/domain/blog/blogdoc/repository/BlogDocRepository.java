package com.back.domain.blog.blogdoc.repository;

import com.back.domain.blog.blogdoc.document.BlogDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogDocRepository extends ElasticsearchRepository<BlogDoc, Long> {
    List<BlogDoc> findByUserId(Long userId);
}
