package com.back.domain.recommend.recommend.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import static com.back.domain.recommend.recommend.constants.PostConstants.BLOG_INDEX_NAME;
import static com.back.domain.recommend.recommend.constants.PostConstants.SHORLOG_INDEX_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostDocService {
    private final ElasticsearchClient esClient;

    public String getContent(boolean isShorlog, Long postId) {
        final String indexName = (isShorlog) ? SHORLOG_INDEX_NAME : BLOG_INDEX_NAME;
        try {
            GetResponse<Map> response = esClient.get(
                    g -> g.index(indexName).id(postId.toString()),
                    Map.class
            );

            if (response.found() && response.source() != null) {
                return (String) response.source().get("content");
            }
            return null;

        } catch (IOException e) {
            log.error("Elasticsearch 조회 실패: {}_id={}", (isShorlog) ? "shorlog" : "blog", postId, e);
            return null;
        }
    }
}
