package com.back.global.config.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.huggingface.HuggingfaceChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    @Bean
    public ChatClient huggingfaceChatClient(HuggingfaceChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}