package com.back.domain.shorlog.shorlogtts.exception;

import com.back.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TtsErrorCase implements ErrorCase {

    TTS_TOKEN_INSUFFICIENT(HttpStatus.BAD_REQUEST, 1, "TTS 토큰이 부족합니다. 기본 음성으로 재생됩니다."),
    TTS_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 2, "TTS 생성에 실패했습니다."),
    TTS_NOT_FOUND(HttpStatus.NOT_FOUND, 3, "TTS가 생성되지 않았습니다."),
    SHORLOG_NOT_FOUND(HttpStatus.NOT_FOUND, 4, "숏로그를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 5, "사용자를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}

