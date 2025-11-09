package com.back.domain.ai.ai.service;

import com.back.domain.ai.ai.dto.*;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiService {
    private final ChatClient openAiChatClient;

    private final SystemMessage systemMessage = SystemMessage.builder()
            .text("""
                    당신은 전문적인 블로그 콘텐츠 제작을 돕는 '블로그 도우미 AI' 입니다.
                    사용자가 제공하는 글의 내용을 바탕으로, 요청된 기능에 해당하는 결과만 명확하게 출력해야 합니다.
                    
                    사용자가 작성하는 콘텐츠는 두 가지 유형으로 제공됩니다:
                    - 블로그(blog): 상세한 정보 제공을 목적으로 하는 블로그 형식입니다.
                    - 숏로그(shorlog): 짧은 분량의 글(SNS 스타일 콘텐츠)입니다. 문장이 간결하고, 연관된 블로그 내용을 요약하거나 흥미를 유발하는 데 초점을 둡니다.
                    
                    당신의 주요 임무는 다음과 같습니다:
                    1. 제목 추천: 블로그의 핵심 내용을 포괄하고 클릭을 유도할 수 있는 매력적인 제목 3~5개를 추천합니다.
                    2. 해시태그 추천: 추출된 키워드를 기반으로 SNS 공유에 적합한 해시태그 목록 10개를 추천합니다.
                    3. 내용 요약: 블로그 글의 전체 내용을 숏로그 유형으로 요약합니다. 요약문은 200~800자 사이로 작성해야 합니다.
                    4. 키워드 추출: 내용의 주제와 관련성이 높은 핵심 키워드 5~10개를 추출하여 목록 형태로 제시합니다.
                    5. 채팅 기능 (질문/답변): 블로그 내용과 관련된 사용자의 추가 질문에 대해 친절하고 정확하게 답변합니다. 답변 내용은 반드시 마크다운(Markdown) 형식(예: 헤더, 목록, 굵게 등)을 사용하여 구조화해야 합니다.
                    
                    출력 규칙:
                    * 사용자가 요청한 기능에 해당하는 결과만 출력합니다. (예: '키워드 추출해 줘'라고 하면 키워드 목록만 출력)
                    * 이미지 생성, 파일 업로드, 외부 API 호출 등 텍스트를 벗어나는 요청은 거부해야 합니다.
                    * 전문적이면서도 친근한 톤을 유지합니다.
                    * 모든 답변은 명확하고 자연스러운 문체로, 실제 블로그 작성에 바로 쓸 수 있게 작성해야 합니다.
                    * 요청받지 않은 다른 기능의 결과는 절대 추가하지 마십시오.
                    """)
            .build();

    private final OpenAiChatOptions options = OpenAiChatOptions.builder()
            .model("gpt-4o-mini")
            .build();

    Prompt prompt;

    public Object generate(AiAssistReqBody req) {
        String command = switch (req.mode().getValue()) {
            case "title" ->
                    "제목 추천: 주어진 블로그의 본문을 분석하여, 독자의 클릭을 유도하는 매력적인 제목 3개를 JSON 형식(키: titles)으로 추출해 주세요. 설명이나 추가적인 문장은 일절 포함하지 말고, 순수한 JSON 객체만 출력해야 합니다."
                    ;
            case "hashtag" -> String.format(
                    "해시태그 추천: 주어진 콘텐츠 유형(%s)의 본문을 분석하여, SNS 공유에 적합한 해시태그 10개를 JSON 형식(키: hashtags)으로 추출해 주세요. 설명 없이 순수한 JSON 객체만 출력해야 합니다.",
                    req.contentType()
            );
            case "summary" -> String.format(
                    "내용 요약: 주어진 블로그의 본문을 분석하여, 간결하고 흥미를 유발하는 요약(200자~800자 사이)을 작성해 주세요. 요약 결과만 출력해야 합니다.",
                    req.contentType()
            );
            case "keyword" -> String.format(
                    "키워드 추출: 주어진 콘텐츠 유형(%s)의 본문을 분석하여, SEO에 최적화된 핵심 키워드 5개를 JSON 형식(키: keywords)으로 추출해 주세요. 설명 없이 순수한 JSON 객체만 출력해야 합니다.",
                    req.contentType()
            );
            case "chat" -> String.format(
                    "채팅 기능: 사용자의 질문에 친절하고 상세하게 답변해 주세요. 답변은 System Message의 규칙에 따라 마크다운 형식으로 구조화해야 합니다. 질문: \"%s\"",
                    req.message()
            );
            default -> throw new ServiceException("400-1", "지원하지 않는 모드입니다.");
        };

        UserMessage userMessage = UserMessage.builder()
                .text(command)
                .text(req.content())
                .build();

        prompt = Prompt.builder()
                .messages(List.of(systemMessage, userMessage))
                .chatOptions(options)
                .build();

        return switch (req.mode().getValue()) {
            case "title" -> generateTitle();
            case "hashtag" -> generateHashtag();
            case "keyword" -> generateKeyword();
            case "summary", "chat" -> generateContent();
            default -> throw new ServiceException("400-1", "지원하지 않는 모드입니다.");
        };
    }

    private AiTitleResBody generateTitle() {
        return openAiChatClient.prompt(prompt)
                .call()
                .entity(AiTitleResBody.class);
    }

    private AiHashtagResBody generateHashtag() {
        return openAiChatClient.prompt(prompt)
                .call()
                .entity(AiHashtagResBody.class);
    }


    private AiKeywordResBody generateKeyword() {
        return openAiChatClient.prompt(prompt)
                .call()
                .entity(AiKeywordResBody.class);
    }


    private AiContentResBody generateContent() {
        return openAiChatClient.prompt(prompt)
                .call()
                .entity(AiContentResBody.class);
    }
}
