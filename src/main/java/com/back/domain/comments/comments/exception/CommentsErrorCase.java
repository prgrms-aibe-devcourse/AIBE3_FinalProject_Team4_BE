package com.back.domain.comments.comments.exception;

import com.back.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommentsErrorCase implements ErrorCase {

    COMMENT_NOT_FOUND(404, 7001, "댓글을 찾을 수 없습니다."),
    PARENT_COMMENT_NOT_FOUND(404, 7002, "부모 댓글을 찾을 수 없습니다."),
    UNAUTHORIZED_UPDATE(403, 7003, "본인 댓글만 수정할 수 있습니다."),
    UNAUTHORIZED_DELETE(403, 7004,  "본인 댓글만 삭제할 수 있습니다.");

    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String message;

}
