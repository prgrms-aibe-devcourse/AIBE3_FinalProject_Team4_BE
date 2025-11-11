package com.back.domain.ai.ai.splitter;

import org.springframework.ai.transformer.splitter.TextSplitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 토큰 단위로 분할 + overlap 지원
 * 블로그, 마크다운 문서 등 긴 텍스트 RAG용
 */
public class OverlapTokenTextSplitter extends TextSplitter {

    private final int chunkSize; // 한 chunk에 포함할 최대 토큰 수
    private final int overlap;   // 다음 chunk와 겹치는 토큰 수

    public OverlapTokenTextSplitter(int chunkSize, int overlap) {
        if (overlap >= chunkSize) {
            throw new IllegalArgumentException("overlap must be smaller than chunkSize");
        }
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    @Override
    protected List<String> splitText(String text) {
        List<String> chunks = new ArrayList<>();

        // 공백 기준 토큰화
        String[] tokens = text.split("\\s+");
        int start = 0;

        while (start < tokens.length) {
            int end = Math.min(start + chunkSize, tokens.length);
            String chunkText = String.join(" ", Arrays.copyOfRange(tokens, start, end));
            chunks.add(chunkText);

            // overlap 적용
            start = end - overlap;
            if (start < 0) start = 0;
        }

        return chunks;
    }
}