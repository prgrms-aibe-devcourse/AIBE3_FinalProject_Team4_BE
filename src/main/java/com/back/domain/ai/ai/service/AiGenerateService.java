package com.back.domain.ai.ai.service;

import com.back.domain.ai.ai.dto.AiGenerateMultiResultsResponse;
import com.back.domain.ai.ai.dto.AiGenerateRequest;
import com.back.domain.ai.ai.dto.AiGenerateSingleResultResponse;
import com.back.domain.ai.ai.util.ContentExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerateService {
    private final ChatClient openAiChatClient;
    private final OpenAiChatOptions modelOption = OpenAiChatOptions.builder()
            .model("gpt-4o-mini") // "gpt-4o-mini" or "gpt-5-nano" ("gpt-5-nano"는 temperature 1.0만 허용)
            .build();

    // 시스템 프롬프트
    public static final String SYSTEM_BASE_PROMPT = """
                    당신은 전문적인 블로그 콘텐츠 제작을 돕는 '블로그 도우미 AI' 입니다.
            
                    출력 규칙:
                    * 이미지 생성, 파일 업로드, 외부 API 호출 등 텍스트를 벗어나는 요청은 거부해야 합니다.
            
            """;
    private static final String SYSTEM_DETAIL_PROMPT = """
                    * 사용자가 제공하는 글의 내용을 바탕으로, 요청된 기능에 해당하는 결과만 명확하게 출력해야 합니다.
                    * 요청받지 않은 다른 기능의 결과는 절대 추가하지 마십시오.
            
                    사용자가 작성하는 콘텐츠는 두 가지 유형으로 제공됩니다:
                    - 블로그(blog): 상세한 정보 제공을 목적으로 하는 블로그 형식입니다.
                    - 숏로그(shorlog): 짧은 분량의 글(SNS 스타일 콘텐츠)입니다. 문장이 간결하고, 연관된 블로그 내용을 요약하거나 흥미를 유발하는 데 초점을 둡니다.
            
                    당신의 주요 임무는 다음과 같습니다:
                    1. 제목 추천: 블로그의 핵심 내용을 포괄하고 클릭을 유도할 수 있는 매력적인 제목 3~5개를 추천합니다.
                    2. 해시태그 추천: 추출된 키워드를 기반으로 SNS 공유에 적합한 해시태그 목록 10개를 추천합니다.
                    3. 내용 요약: 블로그 글의 전체 내용을 숏로그 유형으로 요약합니다. 요약문은 200~800자 사이로 작성해야 합니다.
                    4. 키워드 추출: 내용의 주제와 관련성이 높은 핵심 키워드 1~5개를 추출하여 목록 형태로 제시합니다.
                    5. 섬네일 문구 추천: 블로그 내용을 기반으로 시선을 끄는 섬네일용 문구를 3~5개 추천합니다.
            
            """;

    // 단일 결과 프롬프트
    private static final String USER_SUMMARY_PROMPT = "[내용 요약]: 주어진 블로그의 본문을 분석하여, 간결하고 흥미를 유발하는 요약(200자~800자 사이)을 작성해 주세요. 요약 결과만 출력해야 합니다.\n";

    // 다중 결과 프롬프트
    private static final String JSON_RESULTS_FORMAT_INSTRUCTION = "JSON 형식(키: results)으로 추출해 주세요. 설명 없이 순수한 JSON 객체만 출력해야 합니다.\n";

    private static final String USER_TITLE_PROMPT = "[제목 추천]: 주어진 블로그의 본문을 분석하여, 독자의 클릭을 유도하는 매력적인 제목 3~5개를 " + JSON_RESULTS_FORMAT_INSTRUCTION;
    private static final String USER_HASHTAG_PROMPT = """
            [해시태그 추천]: 주어진 콘텐츠 유형의 본문을 분석하여, SNS 공유에 적합한 해시태그 10개를 %s
                - '#' 기호는 절대 붙이지 마세요.
                - 한글, 영문, 숫자만 사용하세요.
                - 특수문자나 공백은 절대 포함하지 마세요.
            """.formatted(JSON_RESULTS_FORMAT_INSTRUCTION);

    private static final String KEYWORD_BASE_PROMPT = "[키워드 추출]: 주어진 콘텐츠 유형의 본문을 분석하여, ";
    private static final String USER_KEYWORD_PROMPT = KEYWORD_BASE_PROMPT + "SEO에 최적화된 핵심 키워드 5개를 " + JSON_RESULTS_FORMAT_INSTRUCTION;
    private static final String USER_KEYWORD_FOR_UNSPLASH_PROMPT = KEYWORD_BASE_PROMPT + "Unsplash에서 효과적으로 검색할 수 있는 비주얼 중심 핵심 키워드 1~3개를 추출해 주세요. 키워드는 사물, 장면, 분위기 등 다양한 이미지 유형을 포괄할 수 있는 Unsplash 사진 검색에 적합한 형태여야 합니다. " + JSON_RESULTS_FORMAT_INSTRUCTION;
    private static final String USER_KEYWORD_FOR_GOOGLE_PROMPT = KEYWORD_BASE_PROMPT + "Google 이미지 검색에서 높은 관련성을 가지는 핵심 키워드 1~3개를 " + JSON_RESULTS_FORMAT_INSTRUCTION;

    private static final String USER_THUMBNAIL_TEXT_PROMPT = "[섬네일 문구 추천]: 주어진 콘텐츠 유형의 본문을 분석하여, 콘텐츠의 클릭률(CTR)을 극대화할 수 있는, 섬네일 문구 5가지를 " + JSON_RESULTS_FORMAT_INSTRUCTION;

    public Object generate(AiGenerateRequest req) {
        return switch (req.mode()) {
            case TITLE -> generate(
                    appendUserPrompt(USER_TITLE_PROMPT, req),
                    AiGenerateMultiResultsResponse.class
            );
            case HASHTAG -> generate(
                    appendUserPrompt(USER_HASHTAG_PROMPT, req),
                    AiGenerateMultiResultsResponse.class
            );
            case SUMMARY -> generate(
                    appendUserPrompt(USER_SUMMARY_PROMPT, req),
                    AiGenerateSingleResultResponse.class
            );
            case KEYWORD -> generate(
                    appendUserPrompt(USER_KEYWORD_PROMPT, req),
                    AiGenerateMultiResultsResponse.class
            );
            case KEYWORD_FOR_UNSPLASH -> generate(
                    appendUserPrompt(USER_KEYWORD_FOR_UNSPLASH_PROMPT, req),
                    AiGenerateMultiResultsResponse.class
            );
            case KEYWORD_FOR_GOOGLE -> generate(
                    appendUserPrompt(USER_KEYWORD_FOR_GOOGLE_PROMPT, req),
                    AiGenerateMultiResultsResponse.class
            );
            case THUMBNAIL_TEXT -> generate(
                    appendUserPrompt(USER_THUMBNAIL_TEXT_PROMPT, req),
                    AiGenerateMultiResultsResponse.class
            );
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

        String message = req.message();
        String[] previousResults = req.previousResults();
        String content = ContentExtractor.extractContent(req.content());

        if (message != null && !message.isBlank()) {
            userPrompt.append("[질문]: \n");

            if (previousResults != null && previousResults.length > 0) {
                userPrompt.append("""
                        (새 요청)을 보고,
                        A: (이전 결과)는 아예 무시하고, 새로운 결과를 생성하라는 건지
                        아니면
                        B: (이전 결과)를 바탕으로 더 나은 결과를 생성하라는 건지
                        판단해서 결과를 내라.
                        
                        A인 경우,
                        (이전 결과)에 등장한 단어/표현/키워드/색상/고유명사를 절대 재사용하지 마라.
                        단, (새 요청)이나 [본문]에 같은 단어가 명시된 경우는 예외로 허용한다.
                        
                        B인 경우,
                        (이전 결과)를 유지한 채 추가만 하거나,
                        (이전 결과)에서 일부만 개선/수정/보완하거나,
                        (이전 결과)를 완전히 재구성하라.
                        
                        
                        다음은 (이전 결과)와 (새 요청)이다.
                        """);
                userPrompt.append("(이전 결과): \n");
                for (String previousResult : previousResults) {
                    userPrompt.append(previousResult).append("\n");
                }
                userPrompt.append("\n(새 요청): ");
            }

            userPrompt.append(message).append("\n");
        }

        userPrompt.append("[본문]: \n").append(content);

        return userPrompt.toString();
    }
}
