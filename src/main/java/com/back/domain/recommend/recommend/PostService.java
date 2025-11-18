package com.back.domain.recommend.recommend;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import com.back.domain.shorlog.shorlogdoc.repository.ShorlogDocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PostService {

    private final ShorlogDocRepository shorlogDocRepository;

    @Transactional
    public void createPost(Shorlog post) {
        if (shorlogDocRepository.count() == 0) {

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
                        .createdAt(LocalDateTime.of(2025, 11, 17, 20 - i, 1))
                        .build();

                shorlogDocRepository.save(doc);
            }
        }
    }
}