package com.back.domain.blog.blogdoc.repository;

import com.back.domain.blog.blogdoc.document.BlogDoc;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogDocRepository extends ElasticsearchRepository<BlogDoc, String> {

    List<BlogDoc> findByTitleContainingOrContentContaining(String keyword, String keyword2);

    @Query("""
                {
                    "bool": {
                        "should": [
                            {
                                "match": {
                                    "title": "?0"
                                }
                            },
                            {
                                "match": {
                                    "content": "?0"
                                }
                            }
                        ]
                    }
                }
            """)
    List<BlogDoc> searchByKeyword(String keyword);

    
}
