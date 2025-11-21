package com.back.domain.shared.link.exception;

import com.back.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LinkErrorCase implements ErrorCase {

    SHORLOG_NOT_FOUND(HttpStatus.NOT_FOUND, 1, "숏로그를 찾을 수 없습니다."),
    BLOG_NOT_FOUND(HttpStatus.NOT_FOUND, 2, "블로그를 찾을 수 없습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, 3, "권한이 없습니다."),
    ALREADY_LINKED(HttpStatus.BAD_REQUEST, 4, "이미 연결된 블로그입니다."),
    LINK_NOT_FOUND(HttpStatus.NOT_FOUND, 5, "연결을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}

