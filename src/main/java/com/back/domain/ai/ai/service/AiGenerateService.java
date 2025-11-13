package com.back.domain.ai.ai.service;

import com.back.domain.ai.ai.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiGenerateService {
    private final ChatClient openAiChatClient;
    private final OpenAiChatOptions modelOption = OpenAiChatOptions.builder()
            .model("gpt-4o-mini")
            .build();

    public static final String SYSTEM_BASE_PROMPT = """
                    당신은 전문적인 블로그 콘텐츠 제작을 돕는 '블로그 도우미 AI' 입니다.
            
                    출력 규칙:
                    * 이미지 생성, 파일 업로드, 외부 API 호출 등 텍스트를 벗어나는 요청은 거부해야 합니다.
            
            """;
    private final String SYSTEM_DETAIL_PROMPT = """
                    * 사용자가 제공하는 글의 내용을 바탕으로, 요청된 기능에 해당하는 결과만 명확하게 출력해야 합니다.
                    * 요청받지 않은 다른 기능의 결과는 절대 추가하지 마십시오.
            
                    사용자가 작성하는 콘텐츠는 두 가지 유형으로 제공됩니다:
                    - 블로그(blog): 상세한 정보 제공을 목적으로 하는 블로그 형식입니다.
                    - 숏로그(shorlog): 짧은 분량의 글(SNS 스타일 콘텐츠)입니다. 문장이 간결하고, 연관된 블로그 내용을 요약하거나 흥미를 유발하는 데 초점을 둡니다.
            
                    당신의 주요 임무는 다음과 같습니다:
                    1. 제목 추천: 블로그의 핵심 내용을 포괄하고 클릭을 유도할 수 있는 매력적인 제목 3~5개를 추천합니다.
                    2. 해시태그 추천: 추출된 키워드를 기반으로 SNS 공유에 적합한 해시태그 목록 10개를 추천합니다.
                    3. 내용 요약: 블로그 글의 전체 내용을 숏로그 유형으로 요약합니다. 요약문은 200~800자 사이로 작성해야 합니다.
                    4. 키워드 추출: 내용의 주제와 관련성이 높은 핵심 키워드 5~10개를 추출하여 목록 형태로 제시합니다.
            
            """;

    private final String USER_TITLE_PROMPT = "[제목 추천]: 주어진 블로그의 본문을 분석하여, 독자의 클릭을 유도하는 매력적인 제목 3개를 JSON 형식(키: titles)으로 추출해 주세요. 설명이나 추가적인 문장은 일절 포함하지 말고, 순수한 JSON 객체만 출력해야 합니다. 단, '#' 기호는 붙이지 말고 단어만 출력하세요.\n";
    private final String USER_HASHTAG_PROMPT = """
            [해시태그 추천]: 주어진 콘텐츠 유형의 본문을 분석하여, SNS 공유에 적합한 해시태그 10개를 JSON 형식(키: hashtags)으로 추출해 주세요. 설명 없이 순수한 JSON 객체만 출력해야 합니다.
                - '#' 기호는 붙이지 마세요.
                - 한글, 영문, 숫자만 사용하세요.
                - 특수문자나 공백은 포함하지 마세요.
            """;
    private final String USER_SUMMARY_PROMPT = "[내용 요약]: 주어진 블로그의 본문을 분석하여, 간결하고 흥미를 유발하는 요약(200자~800자 사이)을 작성해 주세요. 요약 결과만 출력해야 합니다.\n";
    private final String USER_KEYWORD_PROMPT = "[키워드 추출]: 주어진 콘텐츠 유형의 본문을 분석하여, SEO에 최적화된 핵심 키워드 5개를 JSON 형식(키: keywords)으로 추출해 주세요. 설명 없이 순수한 JSON 객체만 출력해야 합니다.\n";

    public Object generate(AiGenerateRequest req) {
        return switch (req.mode().getValue()) {
            case "title" -> generate(
                    appendUserPrompt(USER_TITLE_PROMPT, req),
                    AiGenerateTitleResponse.class
            );
            case "hashtag" -> generate(
                    appendUserPrompt(USER_HASHTAG_PROMPT, req),
                    AiGenerateHashtagResponse.class
            );
            case "summary" -> generate(
                    appendUserPrompt(USER_SUMMARY_PROMPT, req),
                    AiGenerateSummaryResponse.class
            );
            case "keyword" -> generate(
                    appendUserPrompt(USER_KEYWORD_PROMPT, req),
                    AiGenerateKeywordResponse.class
            );
            default -> throw new IllegalArgumentException("지원하지 않는 모드입니다.");
        };
    }

    private <T> T generate(String userPrompt, Class<T> clazz) {
        return openAiChatClient.prompt()
                .options(modelOption)
                .system(SYSTEM_BASE_PROMPT + SYSTEM_DETAIL_PROMPT)
                .user(userPrompt)
                .call()
                .entity(clazz);
    }

    private String appendUserPrompt(String modePrompt, AiGenerateRequest req) {
        StringBuilder userPrompt = new StringBuilder();

        userPrompt.append(modePrompt).append("\n")
                .append("[콘텐츠 유형]: ").append(req.contentType()).append("\n");
        if (req.message() != null && !req.message().isBlank()) {
            userPrompt.append("[질문]: ").append(req.message()).append("\n");
        }
        userPrompt.append("[본문]: \n").append(req.content());

        return userPrompt.toString();
    }
}
