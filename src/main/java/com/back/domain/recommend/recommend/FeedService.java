package com.back.domain.recommend.recommend;

import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final ElasticsearchOperations elasticsearchOperations;

    // 기본 메인 피드: 인기 + 최신성 기반 추천 (page 0-based)
    public List<ShorlogDoc> getMainFeed(int page, int size) {

//        NativeQuery query = NativeQuery.builder()
//                .withQuery(q -> q
//                                .functionScore(fs -> fs
//                                                .query(m -> m.matchAll(ma -> ma))
//                                                .functions(f -> f.fieldValueFactor(vf -> vf
//                                                        .field("likeCount")
//                                                        .factor(0.25)
//                                                        .modifier(FieldValueFactorModifier.Sqrt)
//                                                ))
//                                                .functions(f -> f.fieldValueFactor(vf -> vf
//                                                        .field("viewCount")
//                                                        .factor(0.30)
//                                                        .modifier(FieldValueFactorModifier.Log1p)
//                                                ))
//                                                .functions(f -> f.fieldValueFactor(vf -> vf
//                                                        .field("commentCount")
//                                                        .factor(0.10)
//                                                ))
////                                .functions(f -> f.gauss(g -> g
////                                        .field("createdAt") // X
////                                        .origin("now")
////                                        .scale("10d")
////                                        .decay(0.5)
////                                ))
//                                                .scoreMode(FunctionScoreMode.Sum)
//                                                .boostMode(FunctionBoostMode.Sum)
//                                )
//                )
//                .withPageable(PageRequest.of(page, size))
//                .build();

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.scriptScore(ss -> ss
                        .query(q2 -> q2.matchAll(ma -> ma))
                        .script(s -> s
                                .source("""
                double likes = doc['likeCount'].size() == 0 ? 0 : doc['likeCount'].value;
                double views = doc['viewCount'].size() == 0 ? 0 : doc['viewCount'].value;
                double comments = doc['commentCount'].size() == 0 ? 0 : doc['commentCount'].value;

                // createdAt 최신성 점수(가우시안 decay)
                long now = new Date().getTime();
                long created = doc['createdAt'].value.getMillis();
                double hours = (now - created) / 3600000.0;
                double recency = Math.exp(-0.5 * Math.pow(hours / 240.0, 2)); // scale=10일(240h)

                return
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
}
