package com.back.domain.ai.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiIndexService {
    private final VectorStore vectorStore;

    /**
     * 블로그 본문을 청크로 분할하고 Pinecone에 저장합니다.
     */
    public void indexBlog(Integer blogId, String title, String content) {
        // 1. 문서 객체 생성 (메타데이터 포함)
        Document blogDocument = new Document(content,
                Map.of(
                        "blogId", blogId,
                        "title", title
                )
        );

        // 2. 청크 분할 (500 토큰 크기, 30~50~100 토큰 오버랩)
        TokenTextSplitter textSplitter = TokenTextSplitter.builder()
                .withChunkSize(500)
                .build();
        List<Document> chunks = textSplitter.split(blogDocument);

        // 3. 임베딩 및 Pinecone Vector DB에 저장
        // 추가로, WebFlux의 Flux를 사용하여 청크 단위로 병렬/비동기 I/O 처리
        Flux.fromIterable(chunks)
                .flatMap(chunk -> Mono.fromRunnable(() -> vectorStore.add(List.of(chunk)))
                        .subscribeOn(Schedulers.boundedElastic()))
                .doOnError(e -> log.error("벡터 저장 에러", e))
                .subscribe();
    }
}