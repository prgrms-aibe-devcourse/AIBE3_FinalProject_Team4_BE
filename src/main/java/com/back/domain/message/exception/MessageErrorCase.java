package com.back.domain.message.exception;

import com.back.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MessageErrorCase implements ErrorCase {
    MESSAGE_THREAD_NOT_FOUND(HttpStatus.NOT_FOUND, 4041, "존재하지 않는 메시지 스레드입니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, 4042, "메시지 스레드에 접근할 권한이 없습니다."),
    CANNOT_CREATE_THREAD_WITH_SELF(HttpStatus.BAD_REQUEST, 4043, "자기 자신과의 메시지 스레드는 생성할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}
