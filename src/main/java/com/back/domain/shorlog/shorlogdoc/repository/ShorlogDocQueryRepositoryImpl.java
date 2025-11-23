package com.back.domain.shorlog.shorlogdoc.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.back.domain.recommend.search.type.PostType;
import com.back.domain.shorlog.shorlogdoc.dto.SearchShorlogResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository
@RequiredArgsConstructor
public class ShorlogDocQueryRepositoryImpl implements ShorlogDocQueryRepository {

    private final ElasticsearchClient esClient;

    @Override
    public SearchResponse<SearchShorlogResponseDto> searchRecommendShorlogs(Query recommendQuery, int pageNumber, int pageSize) {

        PostType postType = PostType.SHORLOG;

        try {
            return esClient.search(s -> s
                            .index(postType.getIndexName())
                            .query(recommendQuery)
                            .from(pageNumber * pageSize)
                            .size(pageSize)
                            .sort(sort -> sort
                                    .field(f -> f.field("_score").order(SortOrder.Desc))
                            )
                            .source(sf -> sf.filter(fi -> fi.includes(postType.getResultFields()))),
                    SearchShorlogResponseDto.class
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
