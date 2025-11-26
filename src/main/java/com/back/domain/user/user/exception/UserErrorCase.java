package com.back.domain.user.user.exception;

import com.back.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCase implements ErrorCase {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 1001, "사용자를 찾을 수 없습니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, 1002, "이미 사용 중인 닉네임입니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, 1003, "권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}
