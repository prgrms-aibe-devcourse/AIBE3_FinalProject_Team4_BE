package com.back.domain.recommend.recommend.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.get.GetResult;
import co.elastic.clients.elasticsearch.core.mget.MultiGetResponseItem;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.back.domain.recommend.recommend.constants.EmbeddingConstants;
import com.back.domain.recommend.recommend.type.PostType;
import com.back.domain.recommend.recommend.util.ElasticsearchDtoMapper;
import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
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
import java.util.stream.IntStream;

import static com.back.domain.recommend.recommend.constants.PostConstants.BLOG_INDEX_NAME;
import static com.back.domain.recommend.recommend.constants.PostConstants.SHORLOG_INDEX_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient esClient;
    private final ElasticsearchDtoMapper esDtoMapper;

    private final RecentPostService recentPostService;

    // 기본 메인 피드
    public List<ShorlogDoc> getFeedWithNativeQuery(int page, int size) {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.scriptScore(ss -> ss
//                        .query(q2 -> q2.matchAll(ma -> ma))
//                                .query(q2 -> q2.moreLikeThis(mlt -> mlt
//                                        .fields("title", "content", "comments", "hashtags")
//                                        .like(l -> l.text("recentText"))
//                                        .minTermFreq(1)
//                                        .minDocFreq(1)
//                                ))
                                .query(q2 -> q2.multiMatch(mm -> mm
                                        .fields("title", "content", "comments", "hashtags")
                                        .query("1")
                                ))
//                                .query(q2 -> q2.bool(b -> b
//                                        .should(s -> s.moreLikeThis(mlt -> mlt
//                                                .fields("title", "content", "comments", "hashtags")
//                                                .like(l -> l.text(recentText))
//                                        ))
//                                        .should(s -> s.multiMatch(mm -> mm
//                                                .fields("title", "content", "hashtags")
//                                                .query(userKeyword)
//                                        ))
//                                ))
                                .script(s -> s
                                        .source("""
                                                    // 기본 데이터
                                                    double likes = doc['likeCount'].size() == 0 ? 0 : doc['likeCount'].value;
                                                    double views = doc['viewCount'].size() == 0 ? 0 : doc['viewCount'].value;
                                                    double comments = doc['commentCount'].size() == 0 ? 0 : doc['commentCount'].value;
                                                
                                                    long now = new Date().getTime();
                                                    long created = doc['createdAt'].value.getMillis();
                                                    double hours = (now - created) / 3600000.0;
                                                    double recency = Math.exp(-0.5 * Math.pow(hours / 240.0, 2)); // scale=10일(240h)
                                                
                                                    // 최종 점수
                                                    return
                                                        _score * 1.5 +
                                                        Math.sqrt(likes) * 0.25 +
                                                        Math.log1p(views) * 0.30 +
                                                        comments * 0.10 +
                                                        recency * 1.0;
                                                """)
                                )
                ))
                .withPageable(PageRequest.of(page, size))
                .build();

        SearchHits<ShorlogDoc> result = elasticsearchOperations.search(query, ShorlogDoc.class);
        return result.get().map(SearchHit::getContent).toList();
    }

    public void searchKnn3(Long userId, int pageNumber, int pageSize, PostType type) {
        List<Long> recentPostIds = recentPostService.getRecentPosts(userId, PostType.SHORLOG);
        List<String> recentContents = recentPostService.getRecentContents(recentPostIds, PostType.SHORLOG);
        float[] userVector = computeUserVector(recentPostIds);

        final String indexName = (type == PostType.SHORLOG) ? SHORLOG_INDEX_NAME : BLOG_INDEX_NAME;

//        List<Float> userVectorList = ArrayConverter.toFloatList(userVector);
        String vectorContent = IntStream.range(0, userVector.length)
                .mapToObj(i -> String.valueOf(userVector[i]))
                .collect(Collectors.joining(", "));
        log.info("vectorContent: {}, ...", String.valueOf(userVector[0]));

        String jsonQuery = """
                {
                  "knn": {
                    "field": "content_embedding",
                    "query_vector": [%s],
                    "k": 10,
                    "num_candidates": 100
                  }
                }
                """.formatted(vectorContent);

//        Response response = restClient.performRequest(
//                new Request("POST", "/" + indexName + "/_search", Map.of("pretty", "true"),
//                        new StringEntity(json, ContentType.APPLICATION_JSON))
//        );
        RestClientTransport transport = (RestClientTransport) esClient._transport();
        RestClient restClient = transport.restClient();

        Request request = new Request("POST", "/" + indexName + "/_knn_search");
        request.setJsonEntity(jsonQuery);

        try {
            Response response = restClient.performRequest(request);
            String responseBody = EntityUtils.toString(response.getEntity());
            log.info("Elasticsearch Raw Response:\n" + responseBody);
            log.info("userVector magnitude: {}", isVectorValid(userVector) ? "Valid" : "Invalid");
        } catch (IOException e) {
            throw new RuntimeException("Low-Level ES Search Error", e);
        }
    }

    public Page<ShorlogDoc> getPostsOrderByRecommendation(Long userId, int pageNumber, int pageSize, PostType type) {
        List<Long> recentPostIds = recentPostService.getRecentPosts(userId, PostType.SHORLOG);
        List<String> recentContents = recentPostService.getRecentContents(recentPostIds, PostType.SHORLOG, 3);
        float[] userVector = computeUserVector(recentPostIds);

        final String indexName = (type == PostType.SHORLOG) ? SHORLOG_INDEX_NAME : BLOG_INDEX_NAME;

//        List<Float> userVectorList = ArrayConverter.toFloatList(userVector);
        List<Float> userVectorList = IntStream.range(0, userVector.length)
                .mapToObj(i -> userVector[i]) // 각 인덱스의 float 값을 가져와 Float 객체로 박싱
                .collect(Collectors.toList());


        // 매핑 이슈 해결 위해 우선 Map으로 받기
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
                                            // 핫한 주제
                                            .should(sh -> sh.multiMatch(mm -> mm
                                                    .fields("content^2", "hashtags^4", "comments") // TODO 블로그: "title^3"
                                                    .query("1") // TODO
                                                    .type(TextQueryType.BestFields)
                                                    .boost(2.0f) // 3.0f
                                            ))
                                            // 최근 본 게시물 유사도
                                            .should(buildRecentMLTQueries(recentContents))
                                            // 최근 본 게시물은 패널티
//                                            .should(sh -> sh.scriptScore(ss -> ss
//                                                    .query(m -> m.matchAll(ma -> ma))
//                                                    .script(sc -> sc
//                                                            .source("""
//                                                                        double score = _score;
//
//                                                                        if (params.recentIds.contains(doc['id'].value)) {
//                                                                            score = score * 0.1;
//                                                                        }
//                                                                        return score;
//                                                                    """)
//                                                            .params("recentIds", JsonData.of(recentPostIds))
//                                                    )
//                                            ))
                                            // 인기 + 최신도
                                            .should(sh -> sh.scriptScore(ss -> ss
                                                    .query(m -> m.matchAll(ma -> ma))
                                                    .script(sc -> sc
                                                            .source("""
                                                                    double views = doc['viewCount'].empty ? 0 : doc['viewCount'].value;
                                                                    double likes = doc['likeCount'].empty ? 0 : doc['likeCount'].value;
                                                                    double comments = doc['commentCount'].empty ? 0 : doc['commentCount'].value;
                                                                    
                                                                    long now = new Date().getTime();
                                                                    long created = doc['createdAt'].value.getMillis();
                                                                    double hours = (now - created) / 3600000.0;
                                                                    double recency = Math.exp(-0.5 * Math.pow(hours / 240.0, 2)); // scale=10일(240h)
                                                                    
                                                                    return
                                                                        Math.log1p(views) * 0.30 +
                                                                        Math.sqrt(likes) * 0.25 +
                                                                        comments * 0.10 +
                                                                        recency * 1.0;
                                                                    """)
                                                    )
                                            ))

                                            // 5) should에 걸리지 않아도 문서 제외되지 않도록 설정
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

        return convertToPage(response, ShorlogDoc.class, pageNumber, pageSize);
    }

    private List<Query> buildRecentMLTQueries(List<String> recentContents) {
        // String 리스트를 More Like This Query 객체 리스트로 변환합니다.
        return recentContents.stream()
                .map(c -> Query.of(q -> q.moreLikeThis(mlt -> mlt
                        .fields("content", "hashtags")
                        .like(l -> l.text(c))
                        .minTermFreq(1)
                        .minDocFreq(1)
                        .boost(3.0f)
                )))
                .collect(Collectors.toList());
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

    public float[] computeUserVector(List<Long> recentIds) {

        int defaultDim = EmbeddingConstants.EMBEDDING_DIM;

        if (recentIds == null || recentIds.isEmpty()) {
            return new float[defaultDim];
        }

        List<ShorlogDoc> docs;

        try {
            var response = esClient.mget(m -> m
                            .index(SHORLOG_INDEX_NAME)
                            .ids(recentIds.stream().map(String::valueOf).toList()),
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

        int dim = docs.get(0).getContentEmbedding().length;
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

    private boolean isVectorValid(float[] vector) {
        if (vector == null || vector.length == 0) {
            return false;
        }
        // 벡터의 L2 Norm (크기)이 0에 매우 가까운지 확인합니다.
        double magnitudeSquared = 0;
        for (float v : vector) {
            magnitudeSquared += v * v;
        }
        // 0이 아닌 아주 작은 값(epsilon)보다 크면 유효하다고 판단합니다.
        return Math.sqrt(magnitudeSquared) > 1e-6;
    }
}
