package com.back.domain.recommend.recommend;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecommendQueryBuilder {
    public Query buildPersonalizedMLTQuery(
            PostType postType,
            List<UserActivityDto> liked,
            List<UserActivityDto> bookmarked,
            List<UserCommentActivityDto> commented,
            List<UserActivityDto> written
    ) {

        List<String> fields = postType.getSearchFields();
        String indexName = postType.getIndexName();

        List<Query> shouldQueries = new ArrayList<>();

        // 좋아요 기반
        for (UserActivityDto u : liked) {
            float weight = UserActivityType.LIKE.getEffectiveWeight(u.activityAt());
            shouldQueries.add(
                    Query.of(q -> q.moreLikeThis(m -> m
                            .fields(fields)
                            .like(l -> l.document(d -> d
                                    .index(indexName)
                                    .id(u.postId().toString())
                            ))
                            .minTermFreq(1)
                            .minDocFreq(1)
                            .boost(weight)
                    ))
            );
        }

        // 북마크 기반
        for (UserActivityDto u : bookmarked) {
            float weight = UserActivityType.LIKE.getEffectiveWeight(u.activityAt());
            shouldQueries.add(
                    Query.of(q -> q.moreLikeThis(m -> m
                            .fields(fields)
                            .like(l -> l.document(d -> d
                                    .index(indexName)
                                    .id(u.postId().toString())
                            ))
                            .minTermFreq(1)
                            .minDocFreq(1)
                            .boost(weight)
                    ))
            );
        }

        // 댓글 기반
        for (UserCommentActivityDto u : commented) {
            float weight = UserActivityType.COMMENT.getEffectiveWeight(u.activityAt());
            if (u.commentCount() >= 3) weight += 1.0f;
            float finalWeight = weight;
            shouldQueries.add(
                    Query.of(q -> q.moreLikeThis(m -> m
                            .fields(fields)
                            .like(l -> l.document(d -> d
                                    .index(indexName)
                                    .id(u.postId().toString())
                            ))
                            .minTermFreq(1)
                            .minDocFreq(1)
                            .boost(finalWeight)
                    ))
            );
        }

        // 내가 작성한 게시글 기반
        for (UserActivityDto u : written) {
            float weight = UserActivityType.POST.getEffectiveWeight(u.activityAt());
            shouldQueries.add(
                    Query.of(q -> q.moreLikeThis(m -> m
                            .fields(fields)
                            .like(l -> l.document(d -> d
                                    .index(indexName)
                                    .id(u.postId().toString())
                            ))
                            .minTermFreq(1)
                            .minDocFreq(1)
                            .boost(weight)
                    ))
            );
        }

        return Query.of(q -> q.bool(b -> b.should(shouldQueries)));
    }
}
