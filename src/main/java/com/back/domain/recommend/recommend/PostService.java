package com.back.domain.recommend.recommend;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import com.back.domain.shorlog.shorlogdoc.repository.ShorlogDocRepository;
import com.back.domain.shorlog.shorlogdoc.service.ShorlogDocService;
import com.back.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

import static com.back.domain.recommend.recommend.constants.PostConstants.SHORLOG_INDEX_NAME;

@Service
@RequiredArgsConstructor
public class PostService {

    private final ShorlogDocRepository shorlogDocRepository;
    private final ShorlogDocService shorlogDocService;
    private final UserRepository userRepository;

    private final ElasticsearchClient esClient;
    private final EmbeddingService embeddingService;

    @Transactional
    public void createPost(Shorlog post) {
        if (shorlogDocRepository.count() < 20) {
            Random random = new Random();
            for (int i = 0; i < 20; i++) {
                ShorlogDoc doc = ShorlogDoc.builder()
                        .id(String.valueOf(i + 1))
                        .userId((long) (i % 5 + 1))
//                        .title("샘플 게시물 #%d - 스프링부트 팁".formatted(i + 1))
                        .content("이 게시물은 샘플 내용입니다. 예제 번호 %d. JWT, Spring Data, Elasticsearch 연동 예제 포함.".formatted(i + 1))
                        .hashtags(List.of("spring", "java", "tips"))
                        .viewCount(random.nextInt(4000))
                        .likeCount(random.nextInt(500))
                        .commentCount(random.nextInt(50))
                        .createdAt(
                                LocalDateTime.of(2025, 11, 17, 20 - i, 1, 0)
                                        .atZone(ZoneId.of("Asia/Seoul"))
                                        .toInstant()
                        )
                        .build();
                // Elasticsearch에서 DateFormat.date_time은 나노초 (9 자리)를 지원하지 않음
                // 최대 millisecond (3 자리)까지만 가능

                shorlogDocRepository.save(doc);
            }
        }


    }

    public void index(ShorlogDoc doc) throws IOException {

        float[] embedding = embeddingService.embed(doc.getContent());
        doc.setContentEmbedding(embedding);

        esClient.index(i -> i
                .index(SHORLOG_INDEX_NAME)
                .id(doc.getId().toString())
                .document(doc)
        );
    }
}