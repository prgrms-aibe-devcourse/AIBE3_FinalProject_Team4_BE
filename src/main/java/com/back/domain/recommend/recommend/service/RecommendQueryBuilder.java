package com.back.domain.recommend.recommend.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import com.back.domain.recommend.search.type.PostType;
import com.back.domain.user.activity.dto.UserActivityDto;
import com.back.domain.user.activity.dto.UserCommentActivityDto;
import com.back.domain.user.activity.type.UserActivityType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class RecommendQueryBuilder {

    // 트렌딩 쿼리
    public Query buildTrendingMLTQuery(PostType postType) {

        long now = System.currentTimeMillis();

        return Query.of(q -> q.scriptScore(ss -> ss
                .query(m -> m.matchAll(ma -> ma))
                .script(sc -> {
                            if (postType == PostType.SHORLOG) {
                                return sc.source("""
                                                double views = doc['viewCount'].size()==0 ? 0 : doc['viewCount'].value;
                                                double likes = doc['likeCount'].size()==0 ? 0 : doc['likeCount'].value;
                                                double comments = doc['commentCount'].size()==0 ? 0 : doc['commentCount'].value;
                                                
                                                long created = doc['createdAt'].value.getMillis();
                                                double hours = (params.now - created) / 3600000.0;
                                                
                                                double engagement =
                                                    Math.log1p(views) * 0.2 +
                                                    Math.sqrt(likes) * 0.6 +
                                                    Math.log1p(comments) * 0.8;
                                                
                                                double halfLife = params.half_life_hours;
                                                double decay = Math.exp(-Math.log(2) * (hours / halfLife));
                                                
                                                double trending = engagement * decay;
                                                return trending * 0.6;
                                                """)
                                        .params(Map.of(
                                                "now", JsonData.of(now),
                                                "half_life_hours", JsonData.of(24.0)
                                        ));
                            }
                            return sc.source("""
                                            double views = doc['viewCount'].size()==0 ? 0 : doc['viewCount'].value;
                                            double likes = doc['likeCount'].size()==0 ? 0 : doc['likeCount'].value;
                                            double bookmarks = doc['bookmarkCount'].size()==0 ? 0 : doc['bookmarkCount'].value;
                                            
                                            return (
                                                Math.log1p(views) * 0.30 +
                                                Math.sqrt(likes) * 0.25 +
                                                Math.sqrt(bookmarks) * 0.28
                                            ) * 0.4;
                                            """);
                        }
                )
        ));
    }

    // 최근 본 게시물 기반 추천 쿼리
    public List<Query> buildRecentViewMLTQueries(PostType postType, List<Long> recentViewPostIds) {

        return IntStream.range(0, recentViewPostIds.size())
                .mapToObj(i -> {
                    // 순서에 따른 weight 적용
                    float weight = switch (i) {
                        case 0 -> 6.0f;
                        case 1 -> 4.0f;
                        case 2 -> 2.0f;
                        default -> 0.1f;
                    };

                    // 최근 본 게시물 유사도
                    Query mltQuery = buildMLTQuery(postType, recentViewPostIds.get(i), weight);

                    return buildFunctionScoreQuery(mltQuery, recentViewPostIds, 0.5); // 최근 본 게시물은 패널티
                })
                .collect(Collectors.toList());
    }

    // 사용자 행동 기반 추천 쿼리
    public List<Query> buildUserActivityMLTQuery(
            PostType postType,
            List<UserActivityDto> likedPosts,
            List<UserActivityDto> bookmarkedPosts,
            List<UserCommentActivityDto> commentedPosts,
            List<UserActivityDto> writtenPosts
    ) {

        List<Query> queries = new ArrayList<>();

        // 좋아요 기반
        for (UserActivityDto u : likedPosts) {
            float weight = UserActivityType.REACTION.getEffectiveWeight(u.activityAt());
            queries.add(
                    buildFunctionScoreQuery(
                            buildMLTQuery(postType, u.postId(), weight),
                            likedPosts.stream().map(UserActivityDto::postId).toList(),
                            0.5
                    )
            );
        }

        // 북마크 기반
        for (UserActivityDto u : bookmarkedPosts) {
            float weight = UserActivityType.REACTION.getEffectiveWeight(u.activityAt());
            queries.add(
                    buildFunctionScoreQuery(
                            buildMLTQuery(postType, u.postId(), weight),
                            bookmarkedPosts.stream().map(UserActivityDto::postId).toList(),
                            0.5
                    )
            );
        }

        // 댓글 기반
        for (UserCommentActivityDto u : commentedPosts) {
            float weight = UserActivityType.COMMENT.getEffectiveWeight(u.activityAt());
            if (u.commentCount() >= 3) weight += 1.0f;
            queries.add(
                    buildFunctionScoreQuery(
                            buildMLTQuery(postType, u.postId(), weight),
                            commentedPosts.stream().map(UserCommentActivityDto::postId).toList(),
                            0.5
                    )
            );
        }

        // 내가 작성한 게시글 기반
        for (UserActivityDto u : writtenPosts) {
            float weight = UserActivityType.POST.getEffectiveWeight(u.activityAt());
            queries.add(
                    buildFunctionScoreQuery(
                            buildMLTQuery(postType, u.postId(), weight),
                            writtenPosts.stream().map(UserActivityDto::postId).toList(),
                            0.0
                    )
            );
        }

        return queries;
    }

    private Query buildMLTQuery(PostType postType, Long postId, float weight) {
        int maxTerms = (postType == PostType.SHORLOG) ? 40 : 25; // 최대 용어 수를 제한

        return Query.of(q -> q.moreLikeThis(m -> m
                .fields(postType.getSearchFields())
                .like(l -> l.document(d -> d
                        .index(postType.getIndexName())
                        .id(postId.toString())
                ))
                .minTermFreq(2)
                .minDocFreq(2)
                .maxQueryTerms(maxTerms)
                .boost(weight)
        ));
    }

    private Query buildFunctionScoreQuery(Query query, List<Long> penaltyIds, double weight) {
        return Query.of(q -> q.functionScore(fs -> fs
                .query(query)
                .functions(f -> f
                        .filter(fq -> fq.terms(t -> t
                                        .field("id")
                                        .terms(tf -> tf.value(penaltyIds.stream()
                                                .map(FieldValue::of).toList()))
                                )
                        )
                        .weight(weight)
                )
                .scoreMode(FunctionScoreMode.Multiply)
        ));
    }
}
