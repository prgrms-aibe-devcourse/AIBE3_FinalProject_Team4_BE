package com.back.domain.shorlog.shorlogtts.dto;

import java.time.LocalDateTime;

public record TtsTokenResponse(
        int token,
        LocalDateTime resetDate
) {
    public static TtsTokenResponse of(int token, LocalDateTime resetDate) {
        return new TtsTokenResponse(token, resetDate);
    }
}

