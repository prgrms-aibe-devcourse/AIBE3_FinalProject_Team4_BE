package com.back.domain.shorlog.shorlogtts.dto;

public record TtsResponse(
        String ttsUrl,
        Integer remainingToken
) {
    public static TtsResponse of(String ttsUrl, Integer remainingToken) {
        return new TtsResponse(ttsUrl, remainingToken);
    }
}

