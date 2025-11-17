package com.back.domain.shorlog.shorlogdoc.repository;

import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShorlogDocRepository extends ElasticsearchRepository<ShorlogDoc, String> {

    @Query("""
        {
            "bool": {
                "should": [
                    {
                        "match": {
                            "content": {
                                "query": ?0,
                                "boost": 2.0,
                                "operator": "or"
                            }
                        }
                    },
                    {
                        "term": {
                            "hashtags": {
                                "value": ?0
                            }
                        }
                    }
                ],
                "minimum_should_match": 1
            }
        }
    """)
    Page<ShorlogDoc> searchByKeywordOrHashtag(String keyword, Pageable pageable);
}

