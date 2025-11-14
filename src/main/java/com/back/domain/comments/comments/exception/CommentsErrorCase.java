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
    UNAUTHORIZED_DELETE(HttpStatus.FORBIDDEN, 7004, "본인 댓글만 삭제할 수 있습니다."),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, 7005, "이 댓글에 대한 권한이 없습니다."),
    COMMENT_LIKE_FORBIDDEN(HttpStatus.FORBIDDEN, 7006, "자신의 댓글에는 좋아요를 누를 수 없습니다."),
    COMMENT_LIKE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, 7007, "이미 좋아요를 누른 댓글입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 7008, "해당 사용자를 찾을 수 없습니다."),
    TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, 7008, "대상 게시글을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    @Override
    public RsData<?> toRsData() {
        return ErrorCase.super.toRsData();
    }
}
