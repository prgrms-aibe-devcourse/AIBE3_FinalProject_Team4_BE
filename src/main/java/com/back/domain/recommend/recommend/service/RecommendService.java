package com.back.domain.recommend.recommend.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.get.GetResult;
import co.elastic.clients.elasticsearch.core.mget.MultiGetResponseItem;
import com.back.domain.recommend.recommend.constants.EmbeddingConstants;
import com.back.domain.recommend.recommend.util.ElasticsearchDtoMapper;
import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.back.domain.recommend.recommend.constants.PostConstants.BLOG_INDEX_NAME;
import static com.back.domain.recommend.recommend.constants.PostConstants.SHORLOG_INDEX_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient esClient;
    private final ElasticsearchDtoMapper esDtoMapper;

    private final RecentViewService recentViewService;

    public List<ShorlogDoc> getFeedWithNativeQuery(int page, int size) {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.scriptScore(ss -> ss
                        .query(q2 -> q2.multiMatch(mm -> mm
                                .fields("title", "content", "comments", "hashtags")
                                .query("1")
                        ))
                        .script(s -> s
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
                .withPageable(PageRequest.of(page, size))
                .build();

        SearchHits<ShorlogDoc> result = elasticsearchOperations.search(query, ShorlogDoc.class);
        return result.get().map(SearchHit::getContent).toList();
    }

    public <T> Page<T> getPostsOrderByRecommend(String guestId, Long userId, int pageNumber, int pageSize, boolean isShorlog, Class<T> targetClazz) {

        List<Long> recentPostIds = recentViewService.getRecentPosts(guestId, userId, isShorlog);
        List<String> recentContents = recentViewService.getRecentContents(isShorlog, recentPostIds, 3);
//        float[] userVector = computeUserVector(recentPostIds);

        final String indexName = (isShorlog) ? SHORLOG_INDEX_NAME : BLOG_INDEX_NAME;

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
        return recentContents.stream()
                .map(recentContent -> {
                    // 최근 본 게시물과 유사한 게시물
                    Query mltQuery = buildMLTQuery(recentContent);

                    // 최근 본 게시물은 패널티
                    return Query.of(q -> q.functionScore(fs -> fs
                            .query(mltQuery)
                            .functions(f -> f
                                    .filter(fq -> fq.terms(t -> t
                                                    .field("id")
                                                    .terms(tf -> tf.value(
                                                            recentPostIds.stream()
                                                                    .map(FieldValue::of)
                                                                    .toList()
                                                    ))
                                            )
                                    )
                                    .weight(0.0)
                            )
                            .scoreMode(FunctionScoreMode.Multiply)
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
                .boost(3.0f)
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

    public float[] computeUserVector(List<Long> recentPostIds) {
        int defaultDim = EmbeddingConstants.EMBEDDING_DIM;

        if (recentPostIds == null || recentPostIds.isEmpty()) {
            return new float[defaultDim];
        }

        List<ShorlogDoc> docs;
        try {
            var response = esClient.mget(m -> m
                            .index(SHORLOG_INDEX_NAME)
                            .ids(recentPostIds.stream().map(String::valueOf).toList()),
                    Map.class
            );

            docs = response.docs().stream()
                    .map(MultiGetResponseItem::result)
                    .filter(Objects::nonNull)

                    .map(GetResult::source)
                    .map(sourceMap -> esDtoMapper.fromSource(sourceMap, ShorlogDoc.class))
                    .filter(Objects::nonNull)
                    .toList();

        } catch (IOException e) {
            log.error("Elasticsearch에서 최근 본 게시물의 임베딩을 조회할 수 없습니다. 기본 벡터로 대체합니다. 상세: {}", e.getMessage(), e);
            return new float[defaultDim];
        }

        if (docs.isEmpty()) {
            return new float[defaultDim];
        }

        int dim = docs.getFirst().getContentEmbedding().length;
        float[] avg = new float[dim];

        for (ShorlogDoc doc : docs) {
            float[] v = doc.getContentEmbedding();
            for (int i = 0; i < dim; i++) {
                avg[i] += v[i];
            }
        }

        for (int i = 0; i < dim; i++) {
            avg[i] /= docs.size();
        }

        return avg;
    }

    private boolean isValidVector(float[] vector) {
        if (vector == null || vector.length == 0) {
            return false;
        }
        // 벡터의 L2 Norm (크기)이 0에 매우 가까운지 확인
        double magnitudeSquared = 0;
        for (float v : vector) {
            magnitudeSquared += v * v;
        }
        // 0이 아닌 아주 작은 값(epsilon)보다 크면 유효하다고 판단
        return Math.sqrt(magnitudeSquared) > 1e-6;
    }
}
