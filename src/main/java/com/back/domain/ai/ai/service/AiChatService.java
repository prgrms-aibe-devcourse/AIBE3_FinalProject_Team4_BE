package com.back.domain.ai.ai.service;

import com.back.domain.ai.ai.dto.AiChatRequest;
import com.back.domain.ai.model.dto.AiModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {
    private final ChatClient openAiChatClient;

    private static final String SYSTEM_DETAIL_PROMPT = """
            * 전문적이면서도 친근한 톤을 유지합니다.
            * 모든 답변은 명확하고 자연스러운 문체로, 실제 블로그 작성에 바로 쓸 수 있게 작성해야 합니다.
            * 답변은 마크다운(Markdown) 형식으로 구조화해야 합니다.
            * 사용자의 [질문]과 [블로그 본문]을 보고, 필요한 경우에만 본문을 활용합니다.
            
            최우선 원칙:
            - 사용자의 요청 의도를 먼저 분류하고, 그 분류에 맞는 답을 합니다.
            
            의도 분류:
            아래 중 하나로 판단합니다.
            A) SMALL_TALK: 인사/감사/짧은 잡담/의미 없는 테스트 (예: "안녕", "고마워", "좋아")
            B) TOOL_BLOCK: 이미지 생성/파일 업로드/외부 API 호출/코드 실행/웹 탐색 등 텍스트 범위를 벗어나는 요청
            C) BLOG_TASK: 블로그 작성/편집/요약/제목/문장 개선/톤 수정/구조화/질의응답 등 본문 기반 작업
            
            행동 규칙:
            1. A) SMALL_TALK 이면:
               - ① 짧은 인사/반응
               - ② [블로그 본문]을 바탕으로 한 “아주 짧은 제안"
               - 위 2문장만 출력하고 즉시 종료합니다.
            
              - '인사' 문장에는 자연스럽게 이모지 0~1개를 포함해도 됩니다.
            
               - 제안은 '다음에 해볼만한 작업 1개'만 말합니다.
               - 제안은 본문에서 바로 손볼 수 있는 “구체적 다음 작업 1개”로 말합니다. (예: 문단 추가, 예시 보강, 구조 정리)
               - 제안 문장은 딱딱한 설명체가 아니라 친근한 추천/코칭 톤으로 씁니다. (“~해보면 좋아요/추천해요/어때요?”)
               - 본문 요약/확장/설명은 금지합니다.
            2. B) TOOL_BLOCK 이면:
               - "텍스트로는 도와줄 수 없어요"처럼 명확히 거절합니다.
            3. C) BLOG_TASK 이면:
               - 먼저 질문에 직답을 한 후, 줄바꿈 등으로 구분을 합니다.
               - 필요한 경우에만 [블로그 본문]을 근거로 작업합니다.
               - 작업 결과는 요청 범위 안에서만 작성합니다.
               - 사용자가 제안을 하면:
                1) "알겠어요" 같은 동의로 끝내지 말고,
                2) 곧바로 [블로그 본문]에 사용자 요청을 적용한 결과를 출력합니다.
            
            """;

    public Flux<String> chatStream(AiChatRequest req) {
        return openAiChatClient.prompt(buildPrompt(req))
                .stream()
                .content();
    }

    public String chatOnce(AiChatRequest req) {
        return openAiChatClient.prompt(buildPrompt(req))
                .call()
                .content();
    }

    private Prompt buildPrompt(AiChatRequest req) {

        OpenAiChatOptions modelOption = buildOptions(req.model());

        SystemMessage systemMessage = SystemMessage.builder()
                .text(AiGenerateService.SYSTEM_BASE_PROMPT)
                .text(SYSTEM_DETAIL_PROMPT)
                .build();

        UserMessage userMessage1 =
                UserMessage.builder()
                        .text("[질문]\n" + req.message() + "\n\n")
                        .build();
        UserMessage userMessage2 =
                UserMessage.builder()
                        .text("---\n\n[블로그 본문] (필요할 때만 참고)\n" + req.content())
                        .build();

        return Prompt.builder()
                .messages(List.of(systemMessage, userMessage1, userMessage2))
                .chatOptions(modelOption)
                .build();
    }

    private OpenAiChatOptions buildOptions(AiModel model) {

        OpenAiChatOptions.Builder optionBuilder = OpenAiChatOptions.builder()
                .model(model.getValue());

        // gpt-5-mini는 temperature 커스텀을 못 받고 1.0만 허용
        if (model == AiModel.GPT_5_MINI) {
            optionBuilder.temperature(1.0);
        }

        return optionBuilder.build();
    }
}