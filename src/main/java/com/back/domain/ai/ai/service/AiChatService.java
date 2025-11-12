package com.back.domain.ai.ai.service;

import com.back.domain.ai.ai.dto.AiChatReqBody;
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
    private final OpenAiChatOptions modelOption = OpenAiChatOptions.builder()
            .model("gpt-4o-mini")
            .build();
    private final VectorStore vectorStore;

    private final String SYSTEM_DETAIL_PROMPT = """
            * 전문적이면서도 친근한 톤을 유지합니다.
            * 모든 답변은 명확하고 자연스러운 문체로, 실제 블로그 작성에 바로 쓸 수 있게 작성해야 합니다.
            * 답변은 마크다운(Markdown) 형식으로 구조화해야 합니다.
            """;

    public String chatOnce(AiChatReqBody req) {
//        return openAiChatClient.prompt(buildPrompt(req))
//                .call()
//                .content();
        String response = openAiChatClient.prompt(buildPrompt(req))
                .call()
                .content();
        log.info("챗봇: {}", response);
        return response;
    }

    public Flux<String> chatStream(AiChatReqBody req) {
        return openAiChatClient.prompt(buildPrompt(req))
                .stream()
                .content();
    }

    private Prompt buildPrompt(AiChatReqBody req) {
        SystemMessage systemMessage = SystemMessage.builder()
                .text(AiGenerateService.SYSTEM_BASE_PROMPT)
                .text(SYSTEM_DETAIL_PROMPT)
                .build();

        UserMessage userMessage = UserMessage.builder()
                .text("[질문]: " + req.message() + "\n\n")
                .text("[블로그 본문]: " + req.content())
                .build();

        return Prompt.builder()
                .messages(List.of(systemMessage, userMessage))
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