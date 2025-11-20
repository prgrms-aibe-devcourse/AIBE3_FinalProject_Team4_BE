package com.back.domain.recommend.recommend;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import com.back.domain.shorlog.shorlogdoc.repository.ShorlogDocRepository;
import com.back.domain.shorlog.shorlogdoc.service.ShorlogDocService;
import com.back.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PostService {

    private final ShorlogDocRepository shorlogDocRepository;
    private final ShorlogDocService shorlogDocService;
    private final UserRepository userRepository;

    private final ElasticsearchClient esClient;
    private final EmbeddingService embeddingService;


    public void deleteAll() {
        shorlogDocRepository.deleteAll();
    }

    public void createPost(Shorlog post) {
        if (shorlogDocRepository.count() < 20) {
            Random random = new Random();
            List<ShorlogDoc> docList = new ArrayList<>();

            for (int i = 0; i < 20; i++) {
                String content;
                if (i < 3) { // id: 1, 2, 3
                    content = "예제 번호 %d. 사진을 찍는다는 것은 프레임 안에 피사체를 배치하는 행위입니다. 의미를 생각하면서 차근차근 찍다보면 사진을 재밌게 찍을 수 있을 것 같습니다. 그럼 핸드폰 사진 잘 찍는법을 알아봅시다. ".formatted(i + 1);
                } else if (i > 17) { // id: 19, 20
                    content = "예제 번호 %d. 오늘은 어떻게 하면 스마트폰 카메라로 전문적인 이미지 느낌이 나게 사진을 찍을 수 있는지에 대한 것을 배워보겠습니다.".formatted(i + 1);
                } else {
                    content = "예제 번호 %d. 이 게시물은 샘플 내용입니다. JWT, Spring Data, Elasticsearch 연동 예제 포함.".formatted(i + 1);
                }

                ShorlogDoc doc = ShorlogDoc.builder()
                        .id(String.valueOf(i + 1))
                        .userId((long) (i % 5 + 1))
//                        .title("샘플 게시물 #%d - 스프링부트 팁".formatted(i + 1))
                        .content(content)
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

                // 임베딩
                doc.setContentEmbedding(embeddingService.embed(doc.getContent()));

                docList.add(doc);

            }


            shorlogDocRepository.saveAll(docList);
        }


    }

}