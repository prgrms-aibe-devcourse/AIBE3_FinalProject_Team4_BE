package com.back.domain.shorlog.shorlogtts.dto;

public record TtsUseResponse(
        boolean success,
        int remainingToken
) {
    public static TtsUseResponse of(boolean success, int remainingToken) {
        return new TtsUseResponse(success, remainingToken);
    }
}

