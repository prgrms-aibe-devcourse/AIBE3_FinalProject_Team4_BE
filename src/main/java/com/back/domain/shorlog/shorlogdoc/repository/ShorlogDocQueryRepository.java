package com.back.domain.shorlog.shorlogdoc.repository;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.back.domain.shorlog.shorlogdoc.dto.SearchShorlogResponseDto;

public interface ShorlogDocQueryRepository {
    SearchResponse<SearchShorlogResponseDto> searchRecommendShorlogs(Query recommendQuery, int pageNumber, int pageSize);
}
