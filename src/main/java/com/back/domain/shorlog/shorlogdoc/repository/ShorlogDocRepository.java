package com.back.domain.shorlog.shorlogdoc.repository;

import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShorlogDocRepository extends ElasticsearchRepository<ShorlogDoc, String> {

    @Query("""
        {
            "bool": {
                "should": [
                    {
                        "match": {
                            "content": {
                                "query": "?0",
                                "boost": 2.0,
                                "operator": "or"
                            }
                        }
                    },
                    {
                        "match": {
                            "hashtags": {
                                "query": "?0",
                                "boost": 1.5,
                                "operator": "or"
                            }
                        }
                    }
                ],
                "minimum_should_match": 1
            }
        }
    """)
    Page<ShorlogDoc> searchByKeywordOrHashtag(String keyword, Pageable pageable);

    // userId로 해당 사용자의 모든 숏로그 문서 조회
    @Query("""
        {
            "term": {
                "userId": "?0"
            }
        }
    """)
    List<ShorlogDoc> findByUserId(Long userId);
}

