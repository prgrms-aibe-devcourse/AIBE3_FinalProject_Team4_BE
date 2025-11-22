package com.back.domain.recommend.recommend.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import com.back.domain.recommend.recommend.PostType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostDocService {
    private final ElasticsearchClient esClient;

    public String getContent(PostType postType, Long postId) {
        try {
            GetResponse<Map> response = esClient.get(
                    g -> g.index(postType.getIndexName()).id(postId.toString()),
                    Map.class
            );

            if (response.found() && response.source() != null) {
                return (String) response.source().get("content");
            }
            return null;

        } catch (IOException e) {
            log.error("Elasticsearch 조회 실패: {}_id={}", postType, postId, e);
            return null;
        }
    }
}
