package com.back.domain.ai.ai.config;

import org.springframework.ai.model.openai.autoconfigure.OpenAiConnectionProperties;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration(proxyBeanMethods = false)
public class OpenAiPrimaryApiConfig {

    @Bean
    @Primary
    public OpenAiApi openAiApi(
            OpenAiConnectionProperties props,
            @Qualifier("openAiRestClientBuilder") RestClient.Builder restClientBuilder,
            @Qualifier("openAiWebClientBuilder") WebClient.Builder webClientBuilder
    ) {
        return OpenAiApi.builder()
                .baseUrl(props.getBaseUrl())
                .apiKey(props.getApiKey())
                .restClientBuilder(restClientBuilder)
                .webClientBuilder(webClientBuilder)
                .build();
    }
}

