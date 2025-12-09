package com.back.domain.user.mail.exception;

import com.back.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RateLimitException implements ErrorCase {
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, 2001, "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}
