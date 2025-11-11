package com.back.domain.comments.comments.exception;

import com.back.global.exception.ErrorCase;
import com.back.global.rsData.RsData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentsErrorCase implements ErrorCase {

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, 7001, "댓글을 찾을 수 없습니다."),
    PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, 7002, "부모 댓글을 찾을 수 없습니다."),
    UNAUTHORIZED_UPDATE(HttpStatus.FORBIDDEN, 7003, "본인 댓글만 수정할 수 있습니다."),
    UNAUTHORIZED_DELETE(HttpStatus.FORBIDDEN, 7004, "본인 댓글만 삭제할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    @Override
    public RsData<?> toRsData() {
        return ErrorCase.super.toRsData(); // 기본 구현 그대로 사용
    }
}
