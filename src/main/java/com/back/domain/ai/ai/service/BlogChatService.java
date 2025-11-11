package com.back.domain.ai.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BlogChatService { // SearchService // BlogRagService // RAG
    // https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html
    // Spring AI RAG 문서 참고하기

    // 1. Spring이 자동 생성한 ChatClient와 VectorStore를 주입받음
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public BlogChatService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    /**
     * RAG 기반으로 사용자 질문에 답변합니다.
     */
    // findRelevant

    // String query, int topk
    // relatedDocs
    public List<Document> findRelevant(String userQuestion, Integer blogId) {
        // 1. 내가 작성한 문서에 대해서만 검색하도록 필터 조건 생성
        // "owner" 필드가 "MY_BLOG_ID_1234"인 문서만 검색 대상으로 삼음
//        Map<String, Object> filter = Map.of("owner", blogId);
        // 1. Map 대신 String 타입의 Pinecone 필터 표현식 생성
        // JSON 문자열을 직접 만듭니다.
        // 2. 필터가 적용된 검색 요청 객체 생성
        SearchRequest searchRequest = SearchRequest.builder() // 인수를 제거
                .query(userQuestion)
                .topK(3) // 상위 3개
                .filterExpression("owner == %d".formatted(blogId))
                .build();

        // 2. Pinecone에서 관련 문서 검색 (Retrieval)
        // Spring AI는 userQuestion을 자동으로 임베딩하여 Pinecone에 쿼리합니다.
        return vectorStore.similaritySearch(searchRequest); // vectorStore.similaritySearch(userQuestion, 3); // cannot resolve method similaritySearch(String, int)
    }

    public String ragChat(String userQuestion, Integer blogId) {
        List<Document> similarDocuments = findRelevant(userQuestion, blogId);

        // 3. 검색된 문서를 하나의 Context 문자열로 결합 (Augmentation)
        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        // 4. 프롬프트 템플릿 정의
        PromptTemplate promptTemplate = new PromptTemplate("""
                [Context]를 바탕으로 [사용자 질문]에 답변하는 블로그 작성 도우미 AI 입니다.
                
                [Context]
                {context}
                
                [사용자 질문]
                {question}
                """);

        // 5. 최종 프롬프트 생성 및 LLM 호출 (Generation)
        String finalPrompt = promptTemplate.render(Map.of(
                "context", context,
                "question", userQuestion
        ));

        return chatClient.prompt(finalPrompt).call().content(); // chatClient.call(finalPrompt) // cannot resolve method 'call' in 'ChatClient'
    }
}