package com.back.domain.blog.blog.exception;

import com.back.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BlogErrorCase implements ErrorCase {
    BLOG_NOT_FOUND(HttpStatus.NOT_FOUND, 101, "존재하지 않는 블로그 글입니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, 102, "해당 글을 수정/삭제할 권한이 없습니다."),
    INVALID_FORMAT(HttpStatus.BAD_REQUEST, 103, "잘못된 블로그 형식입니다."),

    BLOG_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 199, "블로그 저장 중 오류 발생");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;


    @Override
    public int getCode() {
        return 0;
    }
}
