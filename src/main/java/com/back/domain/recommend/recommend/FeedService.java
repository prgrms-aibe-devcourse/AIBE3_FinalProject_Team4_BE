package com.back.domain.recommend.recommend;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class FeedService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient esClient;
    private final ElasticsearchDtoMapper esDtoMapper;

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

    public Page<ShorlogDoc> getFeedMap(int pageNumber, int pageSize) {
        // 매핑 이슈 해결 위해 우선 Map으로 받기
        SearchResponse<Map> response;

        try {
            response = esClient.search(s -> s
                            .index("app1_shorlogs")
//                            .knn(k -> k.field(...)) //
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
                                                            //                                        // 최근 본 게시물과 유사한 내용
                                                            //                                        .should(sh -> sh.moreLikeThis(mlt -> mlt
                                                            //                                                .fields("content", "hashtags", "comments") // TODO 블로그: "title^3"
                                                            //                                                .like(l -> l.text(lastViewedContent))
                                                            //                                                .minTermFreq(1)
                                                            //                                                .minDocFreq(1)
                                                            //                                                .boost(3.0f)
                                                            //                                        ))
//                                            // 최근 본 게시물과 유사한 내용
//                                            .should(sh -> sh.knn(knn -> knn
//                                                    .field("content_embedding")
//                                                    .queryVector(userInterestVector) // lastViewdVector
//                                                    .k(100) // 찾을 이웃 수: 10
//                                                    .numCandidates(500) // 탐색할 후보군 수 (성능/정확도 트레이드오프)
//                                                    .boost(1.5f) // 가중치
//                                            ))
                                                            // 인기 점수
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
                            .sort(sort -> sort
                                    .field(f -> f.field("_score").order(SortOrder.Desc))
                            ),
                    // 6) 결과에 포함할 필드 지정 (필요한 데이터만 가져와 네트워크 부하 감소)
                    //                        .source(sf -> sf.filter(fi -> fi.includes("id", "title", "excerpt", "createdAt"))),
                    Map.class
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return convertToPage(response, ShorlogDoc.class, pageNumber, pageSize);
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
