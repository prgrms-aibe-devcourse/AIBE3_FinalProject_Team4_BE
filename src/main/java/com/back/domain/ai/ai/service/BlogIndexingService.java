package com.back.domain.ai.ai.service;

import com.back.domain.ai.ai.splitter.OverlapTokenTextSplitter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class BlogIndexingService { // DocumentIndexService
    // BlogEmbeddingService

    // 1. Spring이 자동으로 생성한 VectorStore (PineconeVectorStore)를 주입받음
    private final VectorStore vectorStore;

    public BlogIndexingService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 블로그 본문을 청크로 분할하고 Pinecone에 저장합니다.
     */
    // public void indexDocument(String docId, String text)
    // saveBlog()
    public void indexBlogContent(String blogContent, String blogTitle, int blogId) {

        // 1. 문서 객체 생성 (메타데이터 포함)
        Document blogDocument = new Document(blogContent,
                Map.of(
                        "title", blogTitle, // 이건 빼도 될듯
                        "owner", blogId, // <-- 고유 식별자 추가
                        "source", "MY_BLOG" // 얘도 빼도 될듯
                )
        );

        // 2. 청크 분할 (500 토큰 크기, 100 토큰 오버랩)
        // Spring AI의 유틸리티 클래스입니다.
//        TokenTextSplitter textSplitter = TokenTextSplitter.builder()
//                .withChunkSize(500)       // 청크 크기 설정
//                .build();
//        List<Document> chunks = textSplitter.split(blogDocument);


        OverlapTokenTextSplitter splitter = new OverlapTokenTextSplitter(500, 50);
        List<Document> chunks = splitter.split(blogDocument);


        // 3. 임베딩 및 Pinecone 저장 (Embedding and Storage)
        // vectorStore.add() 호출 시, 자동으로 주입된 EmbeddingModel을 사용하여
        // chunks를 벡터화하고 Pinecone에 HTTP 요청을 보냅니다.

//       // (a)
//        vectorStore.add(chunks); // 임베딩 -> pinecone 벡터 저장 (Spring AI가 내부적으로 EmbeddingClient 호출)

        // (b) WebFlux의 Flux를 이용해 병렬 처리
        Flux.fromIterable(chunks)
                .flatMap(chunk -> Mono.fromRunnable(() -> vectorStore.add(List.of(chunk))))
                .subscribe();
        System.out.println(String.format("'%s' 블로그의 %d개 청크를 Pinecone에 저장 완료.", blogTitle, chunks.size()));
    }
}