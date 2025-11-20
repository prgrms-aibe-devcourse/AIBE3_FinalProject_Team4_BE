package com.back.domain.blog.blogdoc.repository;

import com.back.domain.blog.blogdoc.document.BlogDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogDocRepository extends ElasticsearchRepository<BlogDoc, Long> {

}
