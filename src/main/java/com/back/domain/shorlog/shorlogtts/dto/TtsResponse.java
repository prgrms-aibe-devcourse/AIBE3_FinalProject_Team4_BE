package com.back.domain.shorlog.shorlogtts.dto;

public record TtsResponse(
        String ttsUrl
) {
    public static TtsResponse of(String ttsUrl) {
        return new TtsResponse(ttsUrl);
    }
}

