package com.back.domain.recommend.recommend;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import com.back.domain.user.activity.dto.UserActivityDto;
import com.back.domain.user.activity.type.UserActivityType;
import com.back.domain.user.activity.dto.UserCommentActivityDto;
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
                .script(sc -> sc
                        .source("""
                                double views = doc['viewCount'].size()==0 ? 0 : doc['viewCount'].value;
                                double likes = doc['likeCount'].size()==0 ? 0 : doc['likeCount'].value;
                                double comments = doc['commentCount'].size()==0 ? 0 : doc['commentCount'].value;
                                
                                long created = doc['createdAt'].value.getMillis();
                                double hours = (params.now - created) / 3600000.0;
                                
                                double engagement =
                                    Math.log1p(views) * 0.30 +
                                    Math.sqrt(likes) * 0.25 +
                                    comments * 0.10;
                                
                                double trending = engagement / Math.pow(hours + 2, 1.5);
                                
                                return trending;
                                """)
                        .params(Map.of("now", JsonData.of(now)))
                )
        ));
    }

    // 최근 본 게시물 기반 추천 쿼리
    public List<Query> buildRecentViewMLTQueries(PostType postType, List<Long> recentViewPostIds) {

        return IntStream.range(0, recentViewPostIds.size())
                .mapToObj(i -> {
                    // 순서에 따른 weight 적용
                    float weight = switch (i) {
                        case 0 -> 1.0f;
                        case 1 -> 0.7f;
                        case 2 -> 0.5f;
                        default -> 0.1f;
                    };

                    // 최근 본 게시물 유사도
                    Query mltQuery = buildMLTQuery(postType, recentViewPostIds.get(i), weight);

                    return Query.of(q -> q.functionScore(fs -> fs
                            .query(mltQuery)
                            // 최근 본 게시물은 패널티
                            .functions(f -> f
                                    .filter(fq -> fq.terms(t -> t
                                                    .field("id")
                                                    .terms(tf -> tf.value(recentViewPostIds.stream()
                                                            .map(FieldValue::of).toList()))
                                            )
                                    )
                                    .weight(0.5)
                            )
                    ));
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
            float weight = UserActivityType.LIKE.getEffectiveWeight(u.activityAt());
            queries.add(
                    buildMLTQuery(postType, u.postId(), weight)
            );
        }

        // 북마크 기반
        for (UserActivityDto u : bookmarkedPosts) {
            float weight = UserActivityType.LIKE.getEffectiveWeight(u.activityAt());
            queries.add(
                    buildMLTQuery(postType, u.postId(), weight)
            );
        }

        // 댓글 기반
        for (UserCommentActivityDto u : commentedPosts) {
            float weight = UserActivityType.COMMENT.getEffectiveWeight(u.activityAt());
            if (u.commentCount() >= 3) weight += 1.0f;
            queries.add(
                    buildMLTQuery(postType, u.postId(), weight)
            );
        }

        // 내가 작성한 게시글 기반
        for (UserActivityDto u : writtenPosts) {
            float weight = UserActivityType.POST.getEffectiveWeight(u.activityAt());
            queries.add(
                    buildMLTQuery(postType, u.postId(), weight)
            );
        }

        return queries;
    }

    private Query buildMLTQuery(PostType postType, Long postId, float weight) {
        return Query.of(q -> q.moreLikeThis(m -> m
                .fields(postType.getSearchFields())
                .like(l -> l.document(d -> d
                        .index(postType.getIndexName())
                        .id(postId.toString())
                ))
                .minTermFreq(1)
                .minDocFreq(1)
                .boost(weight)
        ));
    }
}
