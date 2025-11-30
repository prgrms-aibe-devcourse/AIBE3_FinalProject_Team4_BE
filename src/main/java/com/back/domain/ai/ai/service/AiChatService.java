package com.back.domain.ai.ai.service;

import com.back.domain.ai.ai.dto.AiChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {
    private final ChatClient openAiChatClient;
    private final VectorStore vectorStore;

    private static final String SYSTEM_DETAIL_PROMPT = """
            * 전문적이면서도 친근한 톤을 유지합니다.
            * 모든 답변은 명확하고 자연스러운 문체로, 실제 블로그 작성에 바로 쓸 수 있게 작성해야 합니다.
            * 답변은 마크다운(Markdown) 형식으로 구조화해야 합니다.
            
            [작업 지시]
                        - 아래 [질문]에 먼저 답하세요.
                        - 그 다음 [블로그 본문]을 참고하여 필요한 경우 보완/개선/요약을 하세요.
            """;

    public String chatOnce(AiChatRequest req) {
        return openAiChatClient.prompt(buildPrompt(req))
                .call()
                .content();
    }

    public Flux<String> chatStream(AiChatRequest req) {
        return openAiChatClient.prompt(buildPrompt(req))
                .stream()
                .content();
    }

    private Prompt buildPrompt(AiChatRequest req) {
        OpenAiChatOptions modelOption = OpenAiChatOptions.builder()
                .model(req.model().getValue())
                .build();

        SystemMessage systemMessage = SystemMessage.builder()
                .text("""
                        당신은 '블로그 도우미 AI'입니다.
                        
                        **가장 중요한 규칙**
                        1. 사용자의 [질문]에 먼저, 간단히라도 반드시 답한다.
                        2. 그 다음에 [블로그 본문]을 참고하여 요청된 작업을 수행한다.
                        3. 질문이 인사/잡담/짧은 대화라면, 자연스럽게 응답하고 본문 작업은 넘어가라.
                        4. 답변에 “[질문]”, “[블로그 본문]” 같은 섹션 제목을 붙이지 말고, 하나의 자연스러운 글로 작성한다.
                        
                        출력 규칙:
                        * 이미지 생성, 파일 업로드, 외부 API 호출 등 텍스트를 벗어나는 요청은 거부해야 합니다.
                        """)
                .text(SYSTEM_DETAIL_PROMPT)
                .build();

        UserMessage userMessage1 =
                UserMessage.builder()
                        .text("[질문]\n" + req.message())
                        .build();
        UserMessage userMessage2 =
                UserMessage.builder()
                        .text("[블로그 본문]\n" + req.content())
                        .build();

        return Prompt.builder()
                .messages(List.of(systemMessage, userMessage1, userMessage2))
                .chatOptions(modelOption)
                .build();
    }

    /**
     * RAG 기반으로 사용자 질문에 답변합니다.
     */
    public String chatWithRag(Integer id, String message) {
        List<Document> similarDocuments = searchRelevantDocuments(id, message);

        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        SystemMessage systemMessage = SystemMessage.builder()
                .text(AiGenerateService.SYSTEM_BASE_PROMPT)
                .text(SYSTEM_DETAIL_PROMPT)
                .build();

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template("""
                        [Context]를 바탕으로 [질문]에 답변하세요.
                        
                        [질문]
                        {message}
                        
                        [Context]
                        {context}
                        """)
                .build();

        UserMessage userMessage = UserMessage.builder()
                .text(
                        promptTemplate.render(Map.of(
                                "message", message,
                                "context", context
                        ))
                )
                .build();

        return openAiChatClient.prompt()
                .messages(
                        systemMessage,
                        userMessage
                )
                .call()
                .content();
    }

    private List<Document> searchRelevantDocuments(Integer blogId, String query) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(3)
                .filterExpression("blogId == %d".formatted(blogId))
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }
}