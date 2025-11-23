package com.back.domain.recommend.recommend.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.back.domain.recommend.recentview.service.RecentViewService;
import com.back.domain.recommend.search.type.PostType;
import com.back.domain.user.activity.dto.UserActivityDto;
import com.back.domain.user.activity.dto.UserCommentActivityDto;
import com.back.domain.user.activity.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendService {

    private final RecommendQueryBuilder queryBuilder;
    private final RecentViewService recentViewService;
    private final UserActivityService userActivityService;

    public List<Query> getRecommendQueries(String guestId, Long userId, PostType postType) {

        List<Query> recommendQueries = new ArrayList<>();

        // 트렌딩
        Query trendingQuery = queryBuilder.buildTrendingMLTQuery(postType);
        recommendQueries.add(trendingQuery);

        // 최근 본 게시물 기반 유사도
        List<Long> recentViewPostIds = recentViewService.getRecentViewPosts(guestId, userId, postType, 3);

        List<Query> recentViewedQuery = queryBuilder.buildRecentViewMLTQueries(postType, recentViewPostIds);
        recommendQueries.addAll(recentViewedQuery);

        // 사용자 행동 기반 추천 (로그인일 때만)
        if (userId != null && userId > 0) {
            boolean isShorlog = (postType == PostType.SHORLOG);
            List<UserActivityDto> likedPosts = userActivityService.getUserLikedPosts(userId, isShorlog);
            List<UserActivityDto> bookmarkedPosts = userActivityService.getUserBookmarkedPosts(userId, isShorlog);
            List<UserCommentActivityDto> commentedPosts = userActivityService.getUserCommentedPosts(userId, isShorlog);
            List<UserActivityDto> writtenPosts = userActivityService.getUserWrittenPosts(userId, isShorlog);

            List<Query> userActivityQuery = queryBuilder.buildUserActivityMLTQuery(postType, likedPosts, bookmarkedPosts, commentedPosts, writtenPosts);
            recommendQueries.addAll(userActivityQuery);
        }

        return recommendQueries;
    }

}
