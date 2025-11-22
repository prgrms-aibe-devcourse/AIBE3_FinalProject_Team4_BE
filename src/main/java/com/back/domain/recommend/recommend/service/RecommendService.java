package com.back.domain.recommend.recommend.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.back.domain.recommend.recommend.PostType;
import com.back.domain.recommend.recommend.util.ElasticsearchDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient esClient;
    private final ElasticsearchDtoMapper esDtoMapper;

    private final RecentViewService recentViewService;

    public <T> Page<T> getPostsOrderByRecommend(String guestId, Long userId, int pageNumber, int pageSize, PostType postType, Class<T> targetClazz) {

        List<Long> recentPostIds = recentViewService.getRecentPosts(guestId, userId, postType);
        List<String> recentContents = recentViewService.getRecentContents(postType, recentPostIds, 3);
//        float[] userVector = computeUserVector(recentPostIds);

        final String indexName = postType.getIndexName();

////        List<Float> userVectorList = ArrayConverter.toFloatList(userVector);
//        List<Float> userVectorList = IntStream.range(0, userVector.length)
//                .mapToObj(i -> userVector[i]) // 각 인덱스의 float 값을 가져와 Float 객체로 박싱
//                .toList();


        // ElasticsearchClient의 DTO 매핑 이슈 해결 위해 우선 Map으로 받기
        SearchResponse<Map> response;

        try {
            response = esClient.search(s -> s
                            .index(indexName)
//                            // 최근 본 게시물과 유사한 내용
//                            .knn(knn -> knn
//                                    .field("content_embedding")
//                                    .queryVector(userVectorList) // lastViewdVector
//                                    .k(10) // 찾을 이웃 수: 10~100
//                                    .numCandidates(50) // 100~300 탐색할 후보군 수 (성능/정확도 트레이드오프)
//                                    .boost(1.5f) // 가중치 3.0f
//                            )
                            .query(q -> q
                                    .bool(b -> b
                                            .must(m -> m.matchAll(ma -> ma))
                                            // 최근 본 게시물 유사도
                                            .should(buildRecentMLTQueries(recentContents, recentPostIds))
                                            // 트렌딩 점수
                                            .should(sh -> sh.scriptScore(ss -> ss
                                                    .query(m -> m.matchAll(ma -> ma))
                                                    .script(sc -> sc
                                                            .source("""
                                                                    double views = doc['viewCount'].size()==0 ? 0 : doc['viewCount'].value;
                                                                    double likes = doc['likeCount'].size()==0 ? 0 : doc['likeCount'].value;
                                                                    double comments = doc['commentCount'].size()==0 ? 0 : doc['commentCount'].value;
                                                                    
                                                                    long now = new Date().getTime();
                                                                    long created = doc['createdAt'].value.getMillis();
                                                                    double hours = (now - created) / 3600000.0;
                                                                    
                                                                    double engagement =
                                                                        Math.log1p(views) * 0.30 +
                                                                        Math.sqrt(likes) * 0.25 +
                                                                        comments * 0.10;
                                                                    
                                                                    double trending = engagement / Math.pow(hours + 2, 1.5);
                                                                    
                                                                    return trending;
                                                                    """)
                                                    )
                                            ))
                                            // should에 걸리지 않아도 문서 제외되지 않도록 설정
                                            .minimumShouldMatch("0")
                                    )
                            )
                            .from(pageNumber * pageSize)
                            .size(pageSize)
                            // _score: kNN과 Bool Query의 점수 합산
                            .sort(sort -> sort
                                    .field(f -> f.field("_score").order(SortOrder.Desc))
                            )
                            // 6) 결과에 포함할 필드 지정 (필요한 데이터만 가져와 네트워크 부하 감소)
                            .source(sf -> sf.filter(fi -> fi.includes("id", "title", "content", "hashtags", "viewCount", "likeCount", "commentCount", "createdAt"))),
                    Map.class
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return convertToPage(response, targetClazz, pageNumber, pageSize);
    }

    private List<Query> buildRecentMLTQueries(List<String> recentContents, List<Long> recentPostIds) {
        return IntStream.range(0, recentContents.size())
                .mapToObj(i -> {
                    String recentContent = recentContents.get(i);

                    // 순서에 따른 weight 적용
                    float weight = switch (i) {
                        case 0 -> 1.0f;
                        case 1 -> 0.7f;
                        case 2 -> 0.5f;
                        default -> 0.1f;
                    };

                    // 최근 본 게시물과 유사한 게시물
                    Query mltQuery = buildMLTQuery(recentContent);

                    // 최근 본 게시물은 패널티
                    return Query.of(q -> q.functionScore(fs -> fs
                            .query(mltQuery)
                            .boost(weight)
                            .functions(f -> f
                                    .filter(fq -> fq.terms(t -> t
                                                    .field("id")
                                                    .terms(tf -> tf.value(recentPostIds.stream()
                                                            .map(FieldValue::of).toList()))
                                            )
                                    )
                                    .weight(0.5)
                            )
                    ));
                })
                .collect(Collectors.toList());
    }

    private Query buildMLTQuery(String content) {
        return Query.of(q -> q.moreLikeThis(mlt -> mlt
                        .fields("content", "hashtags")
                        .like(l -> l.text(content))
                        .minTermFreq(1)
                        .minDocFreq(1)
        ));
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
