package com.back.domain.recommend.recommend.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.back.domain.recommend.recommend.*;
import com.back.domain.recommend.recommend.util.ElasticsearchDtoMapper;
import com.back.domain.user.activity.dto.UserActivityDto;
import com.back.domain.user.activity.service.UserActivityService;
import com.back.domain.user.activity.dto.UserCommentActivityDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendService {

    private final ElasticsearchClient esClient;
    private final ElasticsearchDtoMapper esDtoMapper;

    private final RecentViewService recentViewService;
    private final UserActivityService userActivityService;
    private final RecommendQueryBuilder queryBuilder;

    public <T> Page<T> getPostsOrderByRecommend(String guestId, Long userId, int pageNumber, int pageSize, PostType postType, Class<T> targetClazz) {

        List<Query> shouldQueries = new ArrayList<>();

        // 트렌딩
        Query trendingQuery = queryBuilder.buildTrendingMLTQuery(postType);
        shouldQueries.add(trendingQuery);

        // 최근 본 게시물 기반 유사도
        List<Long> recentViewPostIds = recentViewService.getRecentViewPosts(guestId, userId, postType, 3);

        List<Query> recentViewedQuery = queryBuilder.buildRecentViewMLTQueries(postType, recentViewPostIds);
        shouldQueries.addAll(recentViewedQuery);

        // 사용자 행동 기반 추천 (로그인일 때만)
        if (userId != null && userId != 0) {
            boolean isShorlog = (postType == PostType.SHORLOG);
            List<UserActivityDto> likedPosts = userActivityService.getUserLikedPosts(userId, isShorlog);
            List<UserActivityDto> bookmarkedPosts = userActivityService.getUserBookmarkedPosts(userId, isShorlog);
            List<UserCommentActivityDto> commentedPosts = userActivityService.getUserCommentedPosts(userId, isShorlog);
            List<UserActivityDto> writtenPosts = userActivityService.getUserWrittenPosts(userId, isShorlog);

            List<Query> userActivityQuery = queryBuilder.buildUserActivityMLTQuery(postType, likedPosts, bookmarkedPosts, commentedPosts, writtenPosts);
            shouldQueries.addAll(userActivityQuery);
        }

        // 최종 쿼리
        Query finalQuery = Query.of(q -> q.bool(b -> {
            b.must(m -> m.matchAll(ma -> ma));
            b.should(shouldQueries);
            b.minimumShouldMatch("0");                   // should 안 걸려도 제외 X
            return b;
        }));

        // ElasticsearchClient의 DTO 매핑 이슈 해결 위해 우선 Map으로 받기
        SearchResponse<Map> response;

        try {
            response = esClient.search(s -> s
                            .index(postType.getIndexName())
                            .query(finalQuery)
                            .from(pageNumber * pageSize)
                            .size(pageSize)
                            .sort(sort -> sort
                                    .field(f -> f.field("_score").order(SortOrder.Desc))
                            ),
                    Map.class
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return convertToPage(response, targetClazz, pageNumber, pageSize);
        // return response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
    }

    private <T> Page<T> convertToPage(SearchResponse<Map> response, Class<T> targetClass, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<T> content = response.hits().hits().stream()
                .map(hit -> esDtoMapper.fromHit(hit, targetClass))
                .filter(Objects::nonNull)
                .toList();

        long totalHits = response.hits().total() != null
                ? response.hits().total().value()
                : content.size();

        return new PageImpl<>(content, pageable, totalHits);
    }
}
