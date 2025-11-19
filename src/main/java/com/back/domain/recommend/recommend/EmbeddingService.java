package com.back.domain.recommend.recommend;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            return new float[0];
        }

        return embeddingModel.embed(text);
    }

}
